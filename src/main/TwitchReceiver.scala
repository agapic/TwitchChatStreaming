// Custom receiver docs at http://spark.apache.org/docs/latest/streaming-custom-receivers.html

package com.agapic

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.Socket
import java.nio.charset.StandardCharsets

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.rdd.RDD
import akka.actor.ActorSystem
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import play.api.libs.json._
import java.util.Properties
import Utils._

/** Example from the Spark documentation; this implements a socket
 *  receiver from scratch using a custom Receiver.
 */
class TwitchReceiver(host: String, port: Int)
  extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) {
  
  def gamesAndChannelHandler(irc: IRC, redis: Redis): Unit = {
     // get list of channels for a game and join them
     val games: List[(String, String)] = new TwitchAPIWrapper().getTopGames()

     games.foreach { game => 
       val name = game._2
       val sanitizedGame: String = name.replace(' ', '+')
       val channels: Set[String] = new TwitchAPIWrapper().getEnglishStreamsForGame(sanitizedGame)
       val channelsToRemove: Set[String] = new Utils().channelsToRemove(channels)
       irc.leaveChannels(channelsToRemove)
       channels.foreach(channel => redis.storeGameInRedis(channel, game))
       irc.joinChannels(channels)
     }
  }
  
  /* twitch_auth.txt should have the form
   * twitch_client_id <client-id>
   * twitch_client_secret <client-secret>
   * twitch_username <twitch-username>
   * twitch_password oauth:<password>
   */

  def setAuth() {
    import scala.io.Source
    for (line <- Source.fromFile("twitch_auth.txt").getLines) {
      val fields = line.split(" ")
      if (fields.length == 2) {
        System.setProperty(fields(0), fields(1))
      }
    }
  }
  
  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Socket Receiver") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
   // There is nothing much to do as the thread calling receive()
   // is designed to stop by itself if isStopped() returns false
  }
  
  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {
    var socket: Socket = null
    var userInput: String = null
    try {
     // Connect to host:port
     socket = new Socket(host, port)
     
     // Until stopped or connection broken continue reading
     val reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))
     val writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))
     val irc: IRC = new IRC(reader, writer)
     val redis: Redis = new Redis
     

     // set Auth
     setAuth()
     
     // send Auth to IRC server
     irc.sendAuth()
     val system = akka.actor.ActorSystem("system")
     system.scheduler.schedule(0 seconds, 600 seconds)(gamesAndChannelHandler(irc, redis))
     
     writer.flush()
     
     userInput = reader.readLine()
     
     while(!isStopped && userInput != null) {
       if (userInput.startsWith("PING")) {
         writer.write("PONG :tmi.twitch.tv\r\n");
         writer.flush( );
       }
       
       store(userInput)
       userInput = reader.readLine()
     }
     reader.close()
     socket.close()

     // Restart in an attempt to connect again when server is active again
     restart("Trying to connect again")
    } catch {
     case e: java.net.ConnectException =>
       // restart if could not connect to server
       restart("Error connecting to " + host + ":" + port, e)
     case t: Throwable =>
       // restart if there is any other error
       restart("Error receiving data", t)
    }
  }
}
 
/** Illustrates using a custom receiver to listen for Apache logs on port 7777
 *  and keep track of the top URL's in the past 5 minutes.
 */
object TwitchReceiver {
  
  def getStopWords(): Set[String] = {
    import scala.collection.mutable.ListBuffer
    val source = scala.io.Source.fromFile("stopwords.txt")
    val stopWords = new ListBuffer[String]()

    val lines = try {
      source.getLines.foreach(stopWords += _) 
    } finally {
      source.close()
    }
    
    return stopWords.toSet

  }
  
  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("TwitchReceiver")
    
    val ssc = new StreamingContext(conf, Seconds(4))
    val redis:Redis = new Redis
    redis.flushAll()
    
    val lines = ssc.receiverStream(new TwitchReceiver("irc.chat.twitch.tv", 6667))

    val stopWords = getStopWords

    // Channels and Message Count
    WordCount.count(ssc, lines, Seconds(3600), Seconds(4)) { (channelMessageCount: RDD[WordCount], time: Time) =>
         if (channelMessageCount.count() > 0) {
         val elements = channelMessageCount.collect()
         for (element <- elements) {
           val channel = element.word
           val count = element.count
            redis.storeMessageCountInRedis(channel, count.toString)
         }
       redis.publish
       }
    }
    
    // Messages and word count
    WordCount.count(ssc, lines, Seconds(3600), Seconds(4), stopWords) { (wordsCount: RDD[WordCount], time: Time) =>
       if (wordsCount.count() > 0) {
       val elements = wordsCount.collect()
       for (element <- elements) {
         val word = element.word
         val count = element.count
         redis.storeTopWordCountsInRedis(word, count.toString)
       }
     redis.publish
     }
    }

/* for cassandra
  import com.datastax.spark.connector._
	import com.datastax.spark.connector.writer._
  metadataKeyValues.foreachRDD( (rdd, time) => {
      rdd.cache();
      rdd.saveToCassandra("agapic", "test", SomeColumns("id","channel", "user", "message")) 
  })
*/
    
/* If interested in using Kafka    
  import kafka.common._
  import kafka.serializer._
  import kafka.message._
  import org.apache.kafka.clients.producer._
  
  import org.apache.spark.streaming.kafka._
  import org.apache.kafka.clients._
  val kafkaParams = Map("metadata.broker.list" -> "localhost:9092")
  List of topics you want to listen for from Kafka
  val topics = "testLogs"
  val t = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
    ssc, kafkaParams, topics).map(_._2)
  dstream.foreachRDD( (rdd, time) => {
        
        val props = new java.util.HashMap[String, Object]()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
             "org.apache.kafka.common.serialization.StringSerializer")
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
              "org.apache.kafka.common.serialization.StringSerializer") 
       val producer = new KafkaProducer[String, String](props)
       val elements = rdd.collect()
       for (element <- elements) {
         val channel = element._1
         val count = element._2 
         val channelCountString:String = "(" + channel + "," + count + ")"
         val data = new ProducerRecord[String, String](topics, channelCountString)
         producer.send(data)
       }
         producer.close()
  })
*/

    ssc.checkpoint("dump/")
    ssc.start()
    ssc.awaitTermination()
  }
}

