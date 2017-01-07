package com.agapic

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, StreamingContext, Time}

import Utils._

case class WordCount(word: String, count: Int)

object WordCount {

  type WordHandler = (RDD[WordCount], Time) => Unit

  def count(sc: SparkContext, lines: RDD[String]): RDD[WordCount] = count(sc, lines, Set())

  def count(sc: SparkContext, lines: RDD[String], stopWords: Set[String]): RDD[WordCount] = {
    val stopWordsVar = sc.broadcast(stopWords)

    val words = prepareMessages(lines, stopWordsVar)

    val wordCounts = words.map(word => (word, 1)).reduceByKey(_ + _).map {
      case (word: String, count: Int) => WordCount(word, count)
    }

    val sortedWordCounts = wordCounts.sortBy(_.word)

    sortedWordCounts
  }

  def count(ssc: StreamingContext,
    lines: DStream[String],
    windowDuration: Duration,
    slideDuration: Duration,
    stopWords: Set[String])
    (handler: WordHandler): Unit = countMessages(ssc, lines, windowDuration, slideDuration, stopWords)(handler)
  
  def count(ssc: StreamingContext,
    lines: DStream[String],
    windowDuration: Duration,
    slideDuration: Duration)
    (handler: WordHandler): Unit = countChannels(ssc, lines, windowDuration, slideDuration)(handler)

  def countMessages(ssc: StreamingContext,
    lines: DStream[String],
    windowDuration: Duration,
    slideDuration: Duration,
    stopWords: Set[String])
    (handler: WordHandler): Unit = {

    val sc = ssc.sparkContext
    val stopWordsVar = sc.broadcast(stopWords)
    val words = lines.transform(prepareMessages(_, stopWordsVar))
    words.print()
    val wordCounts = words.map(x => (x, 1)).reduceByKeyAndWindow(_ + _, _ - _, windowDuration, slideDuration).map {
      case (word: String, count: Int) => WordCount(word, count)
    }

    wordCounts.foreachRDD((rdd: RDD[WordCount], time: Time) => {
      handler(rdd, time)
    })
  }  
    
    
  def countChannels(ssc: StreamingContext,
    lines: DStream[String],
    windowDuration: Duration,
    slideDuration: Duration)
    (handler: WordHandler): Unit = {
    val wordCounts = lines.map(x => (messageMetadata(x)._2, 1))
    .filter(line => line._1 != "error")
    .reduceByKeyAndWindow(_ + _, _ - _, windowDuration, slideDuration).map {
      case (word: String, count: Int) => WordCount(word, count)
    }

    wordCounts.foreachRDD( (rdd, time) => {
      handler(rdd, time)
    })
  }
  
  private def prepareMessages(lines: RDD[String], stopWords: Broadcast[Set[String]]): RDD[String] = {
    lines.flatMap(messageMetadata(_)._4.split("\\s"))
      .map(_.strip(",").strip(".").toLowerCase)
      .filter(!stopWords.value.contains(_)).filter(!_.isEmpty).filter(_ != "error")
      .filter(_.length > 2)
  }

}