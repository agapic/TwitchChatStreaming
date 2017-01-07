# TwitchChatStreaming

Provides a leaderboard of the top 50 words and top 50 active channels on Twitch.tv. The data is stored in redis
### TODO: create a script to automate the instructions below.
Quick bootstrapping to get running:

1. You can build the source code using `sbt assembly` while in the sbt folder, but a fully compiled JAR is provided for convenience.
2. Download Redis (https://redis.io/download). Ensure that you download the *stable* version. Run it using `redis-server`.
3. Download Node.js https://nodejs.org/en/download/)
4. Download Spark 2.0.0 (http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz). Add `spark/bin` to your PATH.
5. Register your Twitch.tv application for your client_id and client_secret here. (https://blog.twitch.tv/client-id-required-for-kraken-api-calls-afbb8e95f843#.7hu9s24ub)
6. Sign up for a Twitch.tv username to populate the `username` field. Then, to populate the password field, go to this website to automatically generate an oauth token (https://twitchapps.com/tmi/)
6. Run the spark application with `spark-submit TwitchChat-assembly-1.0.jar`
7. In the `web` directory, run `npm install` followed by `npm start`.

8. View the results at `http://localhost:3002`!
