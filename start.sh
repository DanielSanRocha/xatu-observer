#!/bin/sh

java -jar $1 createTables
java -jar $1 createIndex
java -jar $1 start
