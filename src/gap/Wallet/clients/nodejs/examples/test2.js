
var w = require('../lib/wallet');

var client = w.Wallet(12345, 'localhost', 'test');

var halt = function(message) {
    console.log(message);
    process.exit();
}

client.connect(function(err, sessionID) {
    if (err) halt(err);
    console.log('session = ' + sessionID);    
    client.count(function(err, recs) {
        console.log('count = ' + recs);	
		while (recs > 0) {
		    recs >>= 1;
		    (function(n) {
			var start = new Date();
			client.get('key #'+n, function(err, result) {
				var time = new Date() - start;
				console.log('key #'+n + ' = '+ result + ' -> ' + time + ' ms');
			});
		    }(recs));
		}
    });    
});