# Xatu Observer

![xatu](assets/xatu.jpg?raw=true)


Application for monitoring apis, docker images and services.

## Installation

First install mysql and redis in your server. Go in src/main/resources/queries and execute all the queries in the folder in a database named xatu. To create a new user, just insert a new entry in the 'tb_users' table, the password field must be hashed with md5. You can hash a string using this project:

```bash
sbt console
```

Then

```scala
import com.danielsanrocha.xatu.commons.Security
Security.hash("password")
```

## To package in an unique jar

```bash
sbt assembly
```

the generated jar file will be inside the folder 'target/scala-2.13'.

## To generate unit test coverage

```bash
sbt clean coverage test
sbt coverageReport
```

Made with Love
