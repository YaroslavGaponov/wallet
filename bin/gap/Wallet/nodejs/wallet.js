
var util = require('util');
var net = require('net');
var ee = require('events').EventEmitter;


function getId() {
    return 'id' + Math.floor(Math.random() * 999999999999999999999);
}

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
                
        self.subscribers[frameId] = function(frame) {
            self.session = frame.getParam('session');
            return callback();
        };
        
        var chunk = "";
        self.socket.on('data', function(data) {
            chunk += data.toString();
            var frames = chunk.split('\0');
            if (frames.length > 1) {
                chunk = frames.pop();
            
                for (var i=0;i<frames.length; i++) {
                    var frame = Frame.parse(frames[i]);
                    self.emit(frame.command, frame);
                }
            }
        });
                
        self.on('MESSAGE', function(frame) {
            var frameID = frame.getParam('frameID');
            if (this.subscribers[frameID]) {
                var callback = this.subscribers[frameID];
                delete this.subscribers[frameID];
                return callback(frame);
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



