#!/usr/bin/bash

java -jar $1 createTables
java -jar $1 createIndex
java -jar $1 createUser < $2
java -jar $1 start
