
var w = require('./wallet');

var client = w.Wallet(1121, 'kbp1-dhp-F37307', 'test');

client.connect(function() {
    console.log('session = ' + client.session);    
    client.count(function(frame) {
        console.log('count = ' + frame.getParam('result')); 
    
        for (var i=0;i<1000000;i++) {
            client.set("gap1235 key #"+i, "gap1235 value #"+i, function(frame) {
                console.log(frame.getParam('result'));
                if (i == 1000000) {
                        client.count(function(frame) {
                            console.log('count = ' + frame.getParam('result')); 
                        });
                }
            });
        }
    });    
});