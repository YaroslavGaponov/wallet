
var w = require('../lib/wallet');

var client = w.Wallet(12345, 'kbp1-dhp-F37307', 'test');

client.connect(function() {
    console.log('session = ' + client.session);    
    client.count(function(frame) {
        console.log('count = ' + frame.getParam('result')); 

        for (var i=0;i<1000000;i++) {
            (function(n) {
                client.set("key #"+n, "value #"+n, function(frame) {
                    if (n == 999999) {
                            client.count(function(frame) {
                                console.log('count = ' + frame.getParam('result')); 
                            });
                    }
                });
            })(i);
        }
    });    
});