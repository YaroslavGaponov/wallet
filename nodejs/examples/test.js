#!/usr/bin/env node

/*
# Preconditions
1. create database file: 
	java -jar ./bin/build.jar ./data/test 1000000
2. run database server:
	java -jar ./bin/wallet.jar 12345 ./data
*/

var w = require('../lib/wallet');

var size = 10000;

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
	var counter = 0;
        for (var i=0;i<size;i++) {
            (function(n) {
                client.set('key #' + n, 'value #' + n, function(err, status) {
		    counter++;
		    if ((counter % (size>>2)) == 0) console.log(counter + ' records inserted');
                    if (counter == size) {
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