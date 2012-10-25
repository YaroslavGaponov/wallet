
var w = require('../lib/wallet');

var client = w.Wallet(12345, 'kbp1-dhp-F37307', 'test');

client.connect(function() {
    console.log('session = ' + client.session);    
    client.count(function(frame) {
        console.log('count = ' + frame.getParam('result')); 
		var start = new Date();
		client.get("key #"+1000, function(frame) {
			var time = new Date() - start;
			console.log(frame.getParam('result') + ' -> ' + time + ' ms');
		});
    });    
});