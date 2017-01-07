name := "TwitchChat"

version := "1.0"

organization := "com.agapic"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
"org.apache.spark" %% "spark-core" % "2.0.0" % "provided",
"org.apache.spark" %% "spark-streaming" % "2.0.0" % "provided",
"redis.clients" % "jedis" % "2.9.0",
"com.typesafe.play" % "play-json_2.11" % "2.6.0-M1",
"com.typesafe.play" % "play-functional_2.11" % "2.6.0-M1",
"org.scalaj" % "scalaj-http_2.11" % "2.3.0",
"com.typesafe.akka" % "akka-actor_2.11" % "2.4.16"
)
