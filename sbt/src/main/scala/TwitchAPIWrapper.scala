package com.agapic
import play.api.libs.json._
import play.api.libs.functional._
import scalaj.http._


class TwitchAPIWrapper {
  val clientId: String = System.getProperty("twitch_client_id")
  def idAndNameTuple(game: JsObject): (String, String) = {
    val gameId = (game \ "game" \ "_id").as[Long].toString
    val gameName = (game \ "game" \ "name").as[String]
    return (gameId, gameName)
  }
  
  def getEnglishStreamsForGame(game: String): Set[String] = {
    val response: HttpResponse[String] = 
      Http("https://api.twitch.tv/kraken/streams?game=" + game)
      .header("Client-ID", clientId).asString
    val responseBody = response.body
    val json: JsValue = Json.parse(responseBody)
    val streams = (json \\ "streams")(0).as[List[JsObject]]
    val names = streams
    .filter(stream => (stream \ "channel" \ "language").as[String] == "en")
    .map( stream => (stream \ "channel" \ "name").as[String] )

    return names.toSet
  }
  
  def getTopGames(): List[(String, String)] = {
    val response: HttpResponse[String] = 
      Http("https://api.twitch.tv/kraken/games/top")
      .header("Client-ID", clientId).asString
    val responseBody = response.body
    val json: JsValue = Json.parse(responseBody)
    val games = (json \\ "top")(0).as[List[JsObject]]
    val namesAndIds: List[(String, String)] = games.map(game => idAndNameTuple(game) )
    return namesAndIds
  }
  
}