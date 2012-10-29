/*
# Preconditions
1. create database files:
	java -jar ./bin/build.jar ./data/test1 1000000
	java -jar ./bin/build.jar ./data/test2 1000000
	java -jar ./bin/build.jar ./data/test3 1000000	
	java -jar ./bin/build.jar ./data/test4 1000000
	java -jar ./bin/build.jar ./data/test5 1000000
2. run database server:
	java -jar ./bin/wallet.jar 12345 ./data
*/


var w = require('../lib/wallet');

var size = 1000000;

var client = w.WalletShard(12345, 'localhost', ['test1', 'test2', 'test3', 'test4', 'test5']);

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
	var counter = 0;
	var start = new Date();
        for (var i=0; i<size; i++) {
            (function(n) {
                client.set('key #' + n, 'value #' + n, function(err, status) {                    
		    counter++;

		    if ((counter % (size>>2)) === 0) {
			console.log(counter + ' records inserted');
		    }

                    if (counter == size) {		
			var time = new Date() - start;
			console.log('speed ' + (counter/time) + ' recs/ms');
	
                            client.count(function(err, recs) {
				if (err) halt(err);
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