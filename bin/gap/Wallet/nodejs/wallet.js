
var util = require('util');
var net = require('net');


var client = net.connect(1117,'kbp1-dhp-F37307', function() {
    console.log('connected');
    client.write('CONNECT\n');

    client.on('data', function(data) {
        console.log(data.toString());
    });
});
