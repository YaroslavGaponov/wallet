
var util = require('util');
var net = require('net');
var ee = require('events').EventEmitter;


function getId() {
    return 'id' + Math.floor(Math.random() * 999999999999999999999);
}


function hashCode(key) {
    var hash = 0;
    for (var i=0; i<key.length; i++) {
        hash = ((hash << 5) - hash) + key.charCodeAt(i);
        hash = hash & hash;
    }    
    return hash & 0xffff;
}

/*
 Wallet
*/
var Wallet = module.exports.Wallet = function(port, host, database) {
    if (this instanceof Wallet) {
        this.port = port || '3678';
        this.host = host || '0.0.0.0';
        this.database = database || 'wallet';
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
                
        var frame = Frame('CONNECT');
        var frameId = getId();        
        frame.addParam('frameID', frameId);
        frame.addParam('database', self.database);        
        self.socket.write(frame.getBytes());
                
        self.subscribers[frameId] = function(err, result) {
            self.session = result;
            return callback(err, result);
        };
        
        var chunk = '';
        self.socket.on('data', function(data) {
            chunk += data.toString();
            var frames = chunk.split('\0');
            if (frames.length > 1) {
                chunk = frames.pop();            
                for (var i=0;i<frames.length; i++) {
                    var frame = Frame.parse(frames[i]);
                    self.emit(frame.command, frame.getParam('frameID'), frame.getParam('result'));
                }
            }
        });
                
        self.on('ANSWER', function(frameID, result) {
            if (self.subscribers[frameID]) {
                var callback = self.subscribers[frameID];                
                callback(null, result);
	        delete this.subscribers[frameID];
	        return;
            }
        });

        self.on('ERROR', function(frameID, err) {
            if (frameID) {
                if (self.subscribers[frameID]) {
                    var callback = self.subscribers[frameID];                    
                    callback(err, null);
		    delete self.subscribers[frameID];
		    return;
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


Wallet.prototype.set = function(key, value, callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('SET');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    frame.addParam('key', key);
    frame.addParam('value', value);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.get = function(key, callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('GET');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    frame.addParam('key', key);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.exists = function(key, callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('EXISTS');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    frame.addParam('key', key);
    this.socket.write(frame.getBytes());
}


Wallet.prototype.remove = function(key, callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('REMOVE');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    frame.addParam('key', key);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.count = function(callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('COUNT');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.start = function(callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('START');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.commit = function(callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('COMMIT');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.rollback = function(callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('ROLLBACK');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    this.socket.write(frame.getBytes());
}


Wallet.prototype.disconnect = function(callback) {
    var frameId = getId();
    if (typeof callback === 'function') {
        this.subscribers[frameId] = callback;
    }
    var frame = new Frame('DISCONNECT');
    frame.addParam('frameID', frameId);
    frame.addParam('session', this.session);
    this.socket.write(frame.getBytes());
}

Wallet.prototype.onerror = function(callback) {
    this.subscribers['GlobalErrorHandler'] = callback;
}


/*
 Frame
*/

var Frame = module.exports.Frame = function(command) {
    if (this instanceof Frame) {
        this.command = command;
        this.params = {};
    } else {
        return new Frame(command);
    }
}

Frame.prototype.addParam = function(name, value) {
    this.params[name] = value;
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
    var index = hashCode(key) % this.shards.length;    
    this.shards[index].set(key, value, callback);    
}

WalletShard.prototype.get = function(key, callback) {
    var index = hashCode(key) % this.shards.length;
    this.shards[index].get(key, callback);    
}


WalletShard.prototype.exists = function(key, callback) {
    var index = hashCode(key) % this.shards.length;
    this.shards[index].exists(key, callback);    
}


WalletShard.prototype.remove = function(key, callback) {
    var index = hashCode(key) % this.shards.length;
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
