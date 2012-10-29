/*
# Preconditions
1. create database file: 
	java -jar ./bin/build.jar ./data/test 1000000
2. run database server:
	java -jar ./bin/wallet.jar 12345 ./data
*/

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

	client.set('test1key', 'test1v');
	client.set('test2key', 'test2v');

	client.start();
	client.set('test3key', 'test3v');	
	client.commit();

	client.start();
	client.remove('test1key');
	client.set('test4key', 'test4v');
	client.set('test5key', 'test5v');
	client.rollback(function() {
                client.count(function(err, recs) {
                    console.log('count = ' + recs); 
		});
	});

    });    
});