# Xatu Observer

![xatu](assets/xatu.jpg?raw=true)
![screenshot](assets/screenshot.png?raw=true)


Application for monitoring apis, docker images and services. Also check [xatu-observer-web](https://github.com/DanielSanRocha/xatu-observer-web) for a web frontend to this service.

## Introduction

xatuobserver + [xatuobserver-web](github.com/DanielSanRocha/xatu-observer-web)) is a simple stack for monitoring docker containers, services and APIs.
It has built-in logging collection (powered by ElasticSearch) with a search command for quicker debugging.
It has Telegram integration, after creating a new bot you can configure to be notified of any down resource via Telegram!

## Installation

First install mysql(5.7), elasticsearch (version 7) and redis in your server. Download the latest jar file in the releases page. Create a empty database and export the following environment variables replacing with the values for your server: 

```bash
MYSQL_URL="jdbc:mysql://localhost:3306/xatu?autoReconnect=true&useSSL=false"
MYSQL_USER="root"
MYSQL_PASSWORD="root"
REDIS_HOST="localhost"
REDIS_PORT="6379"
ELASTIC_SEARCH_HOST="localhost"
ELASTIC_SEARCH_PORT="9200"
ELASTIC_SEARCH_LOG_INDEX="xatu-logs"
TELEGRAM_BOT_TOKEN=...
TELEGRAM_CHAT_ID=...
```

check https://core.telegram.org/bots/tutorial for details in getting a TELEGRAM_BOT_TOKEN and a TELEGRAM_CHAT_ID. 

After  that run 
```bash
java -jar xatu-observer-assembly-{version}.jar createTables
java -jar xatu-observer-assembly-{version}.jar createIndex
```

You can start the server with

```bash
java -Xms256m -Xmx512m -Xss2m -Dscala.concurrent.context.numThreads=40 -Dscala.concurrent.context.maxThreads=200 -jar xatu-observer-assembly-{version}.jar start
```

You can transform this in a service, copy the jar file to your server and create .service file, like this:

```
[Unit]
Description=XatuObserver

[Service]
Restart=always
RuntimeMaxSec=300

Environment="PORT=<PORT>"
Environment="MYSQL_USER=<MYSQL_USER>"
Environment="MYSQL_PASSWORD=<MYSQL_PASSWORD>"
...
User=root
ExecStart=/usr/bin/java -Xms256m -Xmx512m -Xss1m -Dscala.concurrent.context.numThreads=40 -Dscala.concurrent.context.maxThreads=100 -jar /path/to/jarfile/xatu-observer-assembly-{version}.jar start
WorkingDirectory=/home/xatu
StandardOutput=append:/var/log/xatu/info.log
StandardError=append:/var/log/xatu/error.log

[Install]
WantedBy=multi-user.target
```

### Creation of an User

To create a new user, just insert a new entry in the 'tb_users' table, the password field must be hashed with md5. You can hash a string using the jar file:

```bash
java -jar xatu-observer-assembly-{version}.jar hash 1234
```
```sql
use xatu;
INSERT INTO tb_users (name,email,password) VALUES (<name>,<email>,<hashed password>);
``` 


## To package in an unique jar with all dependencies

```bash
sbt assembly
```

the generated jar file will be inside the folder 'target/scala-2.13'.

Made With Love ❤
