
var util = require('util');
var net = require('net');
var ee = require('events').EventEmitter;


// create id
var id = 0;
function getId() {
    return 'id' + id++;
}

// commands
var Protocol = {
    // client commands
    CONNECT	:	'CONNECT',
    DISCONNECT	: 	'DISCONNECT',
    SET		: 	'SET',
    GET		: 	'GET',
    COUNT	: 	'COUNT',
    EXISTS	: 	'EXISTS',
    REMOVE	: 	'REMOVE',
    START	:	'START',
    COMMIT	: 	'COMMIT',
    ROLLBACK	: 	'ROLLBACK',
    
    // server commands
    ANSWER	: 	'ANSWER',
    ERROR:	 	'ERROR'    
}

/*
 Wallet
*/
var Wallet = module.exports.Wallet = function(port, host, database) {
    
    if (! port) {
	throw new Error('Port number is not defined.');
    }
    if (! host) {
	throw new Error('Host is not defined.');
    }
    if (! database) {
	throw new Error('Database name is not defined.');
    }
    
    if (this instanceof Wallet) {
	this.database = database;
        this.port = parseInt(port);
        this.host = host;
        this.database = database;
        this.subscribers = {};
    } else {
        return new Wallet(port, host, database);
    }
}

util.inherits(Wallet, ee);

Wallet.prototype.connect = function(callback) {
    var self = this;
    
    this.socket = net.createConnection(this.port, this.host);
    this.socket.on('connect', function() {
        	
        var frame = Frame(Protocol.CONNECT);
        var id = getId();        
        frame.addParam('id', id);
        frame.addParam('database', self.database);        
        self.socket.write(frame.getBytes());
                
        self.subscribers[id] = function(err, sessionId) {
            self.session = sessionId;
            return callback(err, sessionId);
        };
        
        var chunk = '';
        self.socket.on('data', function(data) {
            chunk += data.toString();
            var frames = chunk.split('\0');
            if (frames.length > 1) {
                chunk = frames.pop();            
                for (var i=0;i<frames.length; i++) {
                    var frame = Frame.parse(frames[i]);
                    self.emit(frame.command, frame.getParam('id'), frame.getParam('result'));
                }
            }
        });
                
        self.on(Protocol.ANSWER, function(id, result) {
            if (self.subscribers[id]) {
                var callback = self.subscribers[id];                
	        delete self.subscribers[id];
	        return callback(null, result);
            } 
        });

        self.on(Protocol.ERROR, function(id, err) {
            if (id) {
                if (self.subscribers[id]) {
                    var callback = self.subscribers[id];                    
		    delete self.subscribers[id];
		    return callback(err, null);
                }
            } else {
                if (self.subscribers['GlobalErrorHandler']) {
                    var callback = self.subscribers['GlobalErrorHandler'];
                    return callback(err, null);                    
                }
            }
        });
        
    });
}

Wallet.prototype.createFrame = function(command, callback) {
    var frame = new Frame(command);
    frame.addParam('session', this.session);
    if (typeof callback === 'function') {
	var id = getId();
        this.subscribers[id] = callback;
	frame.addParam('id', id);
    }
    return frame;
}

Wallet.prototype.send = function(frame) {
    this.socket.write(frame.getBytes());
}

Wallet.prototype.set = function(key, value, callback) {
    this.send
    (
	this.createFrame(Protocol.SET, callback)
	      .addParam('key', key)
	      .addParam('value', value)
    );
}

Wallet.prototype.get = function(key, callback) {
    this.send
    (
	this.createFrame(Protocol.GET, callback)
	      .addParam('key', key)
    );
}

Wallet.prototype.exists = function(key, callback) {
    this.send
    (
	this.createFrame(Protocol.EXISTS, callback)
	      .addParam('key', key)
    );
}


Wallet.prototype.remove = function(key, callback) {
    this.send
    (
	this.createFrame(Protocol.REMOVE, callback)
	    .addParam('key', key)
    );
}

Wallet.prototype.count = function(callback) {
    this.send
    (
	this.createFrame(Protocol.COUNT, callback)
    );
}

Wallet.prototype.start = function(callback) {
    this.send
    (
	this.createFrame(Protocol.START, callback)
    );
}

Wallet.prototype.commit = function(callback) {
    this.send
    (
	this.createFrame(Protocol.COMMIT, callback)
    );
}

Wallet.prototype.rollback = function(callback) {
    this.send
    (
	this.createFrame(Protocol.ROLLBACK, callback)
    );
}


Wallet.prototype.disconnect = function(callback) {
    this.send
    (
	this.createFrame(Protocol.DISCONNECT, callback)
    );
}

Wallet.prototype.onerror = function(callback) {
    this.subscribers['GlobalErrorHandler'] = callback;
}


/*
 Frame
*/

var Frame = module.exports.Frame = function(command) {
    if (! Protocol[command]) {
	throw new Error('This command ' + command + ' is not supported.');
    }    
    if (this instanceof Frame) {
        this.command = command;
        this.params = {};
    } else {
        return new Frame(command);
    }
}

Frame.prototype.addParam = function(name, value) {
    this.params[name] = value;
    return this;
}

Frame.prototype.getParam = function(name) {
    return this.params[name];
}


Frame.prototype.getBytes = function()  {
    var frame = this.command + '\n';
    for(var name in this.params) {
        frame += name + ':' + this.params[name] + '\n';
    }
    frame += '\0';
    return frame;
}

Frame.parse = function(bytes) {
    var parts = bytes.split('\n');
    var frame = new Frame(parts[0]);
    for (var i=1; i<parts.length; i++) {
        var kv = parts[i].split(':');
        if (kv.length == 2) {
            frame.addParam(kv[0], kv[1]);
        }
    }    
    return frame;    
}

/*
 WalletShard 
*/
var WalletShard = module.exports.WalletShard = function(port, host, databases) {
    var self = this;
    
    if (this instanceof WalletShard) {
        if (!util.isArray(databases)) {
            databases = [databases];
        }
        this.shards = [];
        databases.forEach(function(database) {
            self.shards.push(new Wallet(port, host, database));
        });
    } else {
        return new WalletShard(port, host, databases);
    }
}


WalletShard.hashCode = function (key) {
    var hash = 0;
    for (var i=0; i<key.length; i++) {
        hash = ((hash << 5) - hash) + key.charCodeAt(i);
        hash = hash & hash;
    }    
    return hash & 0xffff;
}

WalletShard.prototype.connect = function(callback) {
    var self = this;
    var counter = 0;
    this.shards.forEach(function(shard) {
        shard.connect(function(err, result) {
            if (err) return callback(err, null);
            if (++counter === self.shards.length) {
                return callback(null, result);
            }
        });
    });
}

WalletShard.prototype.disconnect = function(callback) {
    var self = this;
    var counter = 0;
    this.shards.forEach(function(shard) {
        shard.disconnect(function(err, result) {
            if (err) return callback(err, null);
            if (++counter === self.shards.length) {
                return callback(null, result);
            }
        });
    });
}


WalletShard.prototype.set = function(key, value, callback) {
    var index = WalletShard.hashCode(key) % this.shards.length;    
    this.shards[index].set(key, value, callback);    
}

WalletShard.prototype.get = function(key, callback) {
    var index = WalletShard.hashCode(key) % this.shards.length;
    this.shards[index].get(key, callback);    
}


WalletShard.prototype.exists = function(key, callback) {
    var index = WalletShard.hashCode(key) % this.shards.length;
    this.shards[index].exists(key, callback);    
}


WalletShard.prototype.remove = function(key, callback) {
    var index = WalletShard.hashCode(key) % this.shards.length;
    this.shards[index].remove(key, callback);    
}

WalletShard.prototype.count = function(callback) {
    var self = this;
    var counter = 0;
    var count = 0;
    this.shards.forEach(function(shard) {
        shard.count(function(err, result) {
            if (err) return callback(err, null);
            count += parseInt(result);            
            if (++counter === self.shards.length) {
                return callback(null, count);
            }
        })
    });
}



WalletShard.prototype.start = function(callback) {
    var self = this;
    var counter = 0;
    this.shards.forEach(function(shard) {
        shard.start(function(err, result) {
            if (err) return callback(err, null);
            if (++counter === self.shards.length) {
                return callback(null, result);
            }
        })
    });
}

WalletShard.prototype.commit = function(callback) {
    var self = this;
    var counter = 0;
    this.shards.forEach(function(shard) {
        shard.commit(function(err, result) {            
            if (err) return callback(err, null);
            if (++counter === self.shards.length) {
                return callback(null, result);
            }
        })
    });    
}

WalletShard.prototype.rollback = function(callback) {
    var self = this;
    var counter = 0;
    this.shards.forEach(function(shard) {
        shard.rollback(function(err, result) {
            if (err) return callback(err, null);
            if (++counter === self.shards.length) {
                return callback(null, result);
            }
        })
    });
}


WalletShard.prototype.onerror = function(callback) {
    var self = this;
    this.shards.forEach(function(shard) {
        shard.onerror(callback);
    });    
}


