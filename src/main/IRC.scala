package com.agapic
import java.net.Socket
import java.io.{BufferedReader, BufferedWriter}

class IRC(val reader: BufferedReader, val writer: BufferedWriter) {
  
  def sendAuth(): Unit = {
     val username = System.getProperty("twitch_username")
     val password = System.getProperty("twitch_password")
     writer.write("PASS " + password + "\r\n")
     writer.write("USER " + username + "\r\n")
     writer.write("NICK " + username + "\r\n")
     writer.flush()
  }
  
  def joinChannels(channels: Set[String]): Unit = {
    val channelsWithHashtags = channels.map(channel => "#" + channel)
    writer.write("JOIN " + channelsWithHashtags.mkString(",") + "\r\n")
    writer.flush()
  }
  
  def leaveChannels(channels: Set[String]): Unit = {
    val channelsWithHashtags = channels.map(channel => "#" + channel)
    writer.write("PART " + channelsWithHashtags.mkString(",") + "\r\n")
    writer.flush()
  }

}

