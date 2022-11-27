ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "xatu-observer"
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

lazy val versions = new {
  val finatra = "21.2.0"
  val finagle = "21.2.0"
  val twitter = "21.2.0"
  val typesafeConfig = "1.3.1"
  val logback = "1.1.7"
  val activation = "1.2.0"
  val scalaUUID = "0.3.1"
  val jedis = "4.3.0"
  val slick = "3.4.1"
  val mysql = "8.0.31"
  val scalaj = "2.4.2"
}

lazy val versionsTest = new {
  val scalatest = "3.2.14"
  val mockito = "3.2.10.0"
  val twitter = "21.3.0"
  val h2 = "2.1.214"
}

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-thrift" % versions.finatra,
  "com.twitter" %% "finagle-http" % versions.finagle,
  "com.typesafe.slick" %% "slick" % versions.slick,
  "com.typesafe.slick" %% "slick-hikaricp" % versions.slick,
  "mysql" % "mysql-connector-java" % versions.mysql,
  "com.typesafe" % "config" % versions.typesafeConfig,
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "com.sun.activation" % "javax.activation" % versions.activation,
  "io.jvm.uuid" %% "scala-uuid" % versions.scalaUUID,
  "redis.clients" % "jedis" % versions.jedis,
  "org.scalaj" %% "scalaj-http" % versions.scalaj
)

Test / parallelExecution := false

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % versionsTest.scalatest % Test,
  "org.scalatestplus" %% "mockito-3-4" % versionsTest.mockito % Test,
  "com.twitter" %% "finatra-jackson" % versionsTest.twitter % Test classifier "tests",
  "com.twitter" %% "finatra-http-server" % versionsTest.twitter % Test classifier "tests",
  "com.twitter" %% "inject-server" % versionsTest.twitter % Test classifier "tests",
  "com.twitter" %% "inject-app" % versionsTest.twitter % Test classifier "tests",
  "com.twitter" %% "inject-core" % versionsTest.twitter % Test classifier "tests",
  "com.twitter" %% "inject-modules" % versionsTest.twitter % Test classifier "tests",
  "com.h2database" % "h2" % versionsTest.h2 % Test
)
