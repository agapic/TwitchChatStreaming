package com.agapic
import redis.clients.jedis._
import java.util.{HashMap, HashSet}

class Redis extends java.io.Serializable  {
  import Redis._
  private val games = new HashSet[(String, String)]
  
  def storeGameInRedis(channel: String, game: (String, String)): Unit = {
    val gameProps: HashMap[String, String] = new HashMap[String, String]();
    gameProps.put("gameId", game._1)
    jedis.hmset("channel:" + channel, gameProps)
    gameProps.put("name", game._2)
    
    if (!games.contains(game)) {
      val gameId = game._1
      jedis.hmset("game:" + gameId, gameProps)
    }
    
    games.add(game)
  }
  
  def storeMessageCountInRedis(channel: String, count: String): Unit = {
    val channelProps: HashMap[String, java.lang.Double] = new HashMap[String, java.lang.Double]();
    val gameId:Long = jedis.hget("channel:" + channel, "gameId").toLong
    channelProps.put("channel:" + channel + ":" + gameId, count.toLong)
    jedis.zadd("messageCounts", channelProps)
  }
  
  def storeTopWordCountsInRedis(word: String, count: String): Unit = {
    val wordProps: HashMap[String, java.lang.Double] = new HashMap[String, java.lang.Double]();
    wordProps.put("word:" + word, count.toLong)
    jedis.zadd("wordCounts", wordProps)
  }
  
  def publish() = {
    jedis.publish("channelCounts", "messageCounts")
    jedis.publish("wordCounts", "wordCounts")
    
    // only keep top 50 counts in memory
    jedis.zremrangeByRank("messageCounts", 0, -51)
    jedis.zremrangeByRank("wordCounts", 0, -51)
  }
  
  def flushAll() = {
    jedis.flushAll()
  }
}

object Redis {
  private val jedis:Jedis = new Jedis("127.0.0.1")
}