# TwitchChatStreaming

Provides a leaderboard of the top 50 words and top 50 active channels on Twitch.tv. The data is stored in redis

Quick bootstrapping to get running:

1. You can build the source code using `sbt assembly` while in the sbt folder, but a fully compiled JAR is provided for convenience.
2. Download Redis (https://redis.io/download). Ensure that you download the *stable* version. Run it using `redis-server`.
3. Download Node.js https://nodejs.org/en/download/)
4. Download Spark 2.0.0 (http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz). Add `spark/bin` to your PATH.
5. Run the spark application with `spark submit TwitchChat-assembly-1.0.jar`
6. In the `web` directory, run npm install and then npm start.

7. View the results!
