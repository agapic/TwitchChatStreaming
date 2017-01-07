package com.agapic

import java.util.regex.Pattern
import java.util.regex.Matcher

class Utils {
    private var channels: Set[String] = Set()
    
    def channelsToRemove(newChannels: Set[String]) = {

      // the private member channels is a list of the old channels
      // `newChannels` contains an updated list of channels
      // when pruning, we want to leave channels that are offline
      val offlineChannels = channels &~ newChannels
      channels = newChannels
      channels
    }
}

object Utils {

  implicit class StringUtils(val value: String) {
    def strip(stripChars: String): String = value.stripPrefix(stripChars).stripSuffix(stripChars)
  }
  
  def twitchChatMessagePattern():Pattern = {
    val username = "(^:(.+)!)(.*)"
    val messageAndChannel = "(PRIVMSG #([a-zA-Z0-9_]+) :(.+))"
    val regex = s"$username$messageAndChannel"
    Pattern.compile(regex)    
  }
  
  def messageMetadata(line: String): (Long, String, String, String) = {
    val pattern = twitchChatMessagePattern()
    val matcher:Matcher = pattern.matcher(line)
    if (matcher.matches()) {
      // channel, user, message
      return (System.currentTimeMillis(), matcher.group(5),matcher.group(2),matcher.group(6))
    }
    return (1, "error", "error", "error")
  }

  
}