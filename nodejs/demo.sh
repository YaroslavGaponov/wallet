#!/bin/bash

mkdir data

java -jar ./bin/build.jar ./data/test 1000000

java -jar ./bin/build.jar ./data/test1 1000000
java -jar ./bin/build.jar ./data/test2 1000000
java -jar ./bin/build.jar ./data/test3 1000000	
java -jar ./bin/build.jar ./data/test4 1000000
java -jar ./bin/build.jar ./data/test5 1000000
