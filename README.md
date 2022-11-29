# Xatu Observer

![xatu](assets/xatu.jpg?raw=true)


Application for monitoring apis, docker images and services. Also check [xatu-observer-web](https://github.com/DanielSanRocha/xatu-observer-web) for a web frontend to this service.

## Installation

First install mysql, elasticsearch (version 7) and redis in your server. Download the latest jar file in the releases page. Create a empty database and export the following environment variables replacing with the values for your server: 

```bash
MYSQL_URL="jdbc:mysql://localhost:3306/xatu?autoReconnect=true&useSSL=false"
MYSQL_USER="root"
MYSQL_PASSWORD="root"
REDIS_HOST="localhost"
REDIS_PORT="6379"
ELASTIC_SEARCH_HOST="localhost"
ELASTIC_SEARCH_PORT="9200"
ELASTIC_SEARCH_LOG_INDEX="xatu-logs"
```

After  that run 
```bash
java -jar xatu-observer-assembly-{version}.jar createTables
java -jar xatu-observer-assembly-{version}.jar createIndex
```

You can start the server with

```bash
java -jar xatu-observer-assembly-{version}.jar start
```

You can transform this in a service, copy the jar file to your server and create .service file, like this:

```
[Unit]
Description=XatuObserver

[Service]
Environment="PORT=<PORT>"
Environment="MYSQL_USER=<MYSQL_USER>"
Environment="MYSQL_PASSWORD=<MYSQL_PASSWORD>"
...
User=root
ExecStart=java -jar /path/to/jarfile/xatu-observer-assembly-{version}.jar start
WorkingDirectory=/home/xatu
StandardOutput=append:/var/log/xatu/info.log
StandardError=append:/var/log/xatu/error.log

[Install]
WantedBy=multi-user.target
```

To create a new user, just insert a new entry in the 'tb_users' table, the password field must be hashed with md5. You can hash a string using the jar file:

```bash
java -jar xatu-observer-assembly-{version}.jar hash 1234
```

## To package in an unique jar with all dependencies

```bash
sbt assembly
```

the generated jar file will be inside the folder 'target/scala-2.13'.

Made With Love ❤
