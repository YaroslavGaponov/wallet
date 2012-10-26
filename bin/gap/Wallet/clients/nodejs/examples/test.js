
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
	if (err) halt(err);
        console.log('count = ' + recs); 

	console.log('inserting ...');
        for (var i=0;i<1000000;i++) {
            (function(n) {
                client.set('key #' + n, 'value #' + n, function(err, status) {                    
		    if ((n % 10000) == 0) console.log(n + ' records inserted');
                    if (n == 999999) {
                            client.count(function(err, recs) {
                                console.log('count = ' + recs);
                                client.disconnect(function() {
                                    process.exit();
                                })
                            });
                    }
                });
            })(i);
        }
    });    
});

client.onerror(function(err) {
    halt(err);
});