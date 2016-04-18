package project.udacity

import net.liftweb.json._
import java.io.BufferedWriter
import java.io.FileWriter

object Scraper {
  val CLIENT_ID = "CLIENT_ID"
  val CLIENT_SECRET = "CLIENT_SECRET"
  
  val categoryIds = Seq(
      "52e81612bcbc57f1066b7a0d",
      "56aa371ce4b08b9a8d57356c",
      "4bf58dd8d48988d117941735",
      "4bf58dd8d48988d11e941735",
      "4bf58dd8d48988d118941735",
      "4bf58dd8d48988d1d8941735",
      "4bf58dd8d48988d119941735",
      "4bf58dd8d48988d1d5941735",
      "4bf58dd8d48988d120941735",
      "4bf58dd8d48988d11b941735",
      "4bf58dd8d48988d11c941735",
      "4bf58dd8d48988d11d941735",
      "56aa371be4b08b9a8d57354d",
      "4bf58dd8d48988d122941735",
      "4bf58dd8d48988d123941735",
      "50327c8591d4c4b30a586d5d",
      "4bf58dd8d48988d121941735",
      "4bf58dd8d48988d11f941735",
      "4bf58dd8d48988d1d4941735"
  )
  
  val venueIdsFile = "venueIds.txt"
  val dataFolder = "data/"
  
  case class Venue(name: String, id: String)
  case class Tip(date: String, text: String)
  case class Photo(prefix: String, suffix: String)
  
  def main(args: Array[String]): Unit = {
    val venues = categoryIds.flatMap(getVenues).toSet
    val writer = new BufferedWriter(new FileWriter(venueIdsFile))
    venues.foreach(venue => writer.write(venue.name + "," + venue.id + "\n"))
    writer.close
    
    println()
    venues.foreach(writeVenueInfo)
  }
  
  def getVenues(category: String) = {
    val url = s"https://api.foursquare.com/v2/venues/search?v=20160402&intent=browse&limit=50&sw=20.591652120829167%2C-103.4446907043457&ne=20.753866576560597%2C-103.23698043823242&categoryId=$category&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET"
    println(s"Scraping $url")
    val jsonResponse = parse(scala.io.Source.fromURL(url).mkString)
    
    val venueNamesObj = jsonResponse \ "response" \ "venues" \ "name"
    val venueIdsObj = jsonResponse \ "response" \ "venues" \ "id"
    (venueNamesObj.children zip venueIdsObj.children).map {
      case (JField(_, JString(name)), JField(_, JString(id))) => Venue(name, id)
      case _ => Venue("", "")
    }
  }
  
  def getTips(venueId: String, offset: Int): List[Tip] = {
    val url = s"https://api.foursquare.com/v2/venues/$venueId/tips?v=20160402&sort=recent&offset=$offset&limit=100&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET"
    println(s"Scraping $url")
    val jsonResponse = parse(scala.io.Source.fromURL(url).mkString)
    
    val tipDatesObj = jsonResponse \\ "createdAt"
    val tipTextsObj = jsonResponse \\ "text"
    val tips = (tipDatesObj.children zip tipTextsObj.children).map {
      case (JField(_, JInt(date)), JField(_, JString(text))) => Tip(date.toString, text.replace("|", ""))
      case _ => Tip("", "")
    }
    
    if(tips.size > 0)
      tips ++: getTips(venueId, offset + 100)
    else
      List()
  }
  
  def getPhoto(venueId: String) = {
    implicit val formats = DefaultFormats
    
    val url = s"https://api.foursquare.com/v2/venues/$venueId/photos?v=20160402&limit=1&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET"
    println(s"Scraping $url")
    val jsonResponse = parse(scala.io.Source.fromURL(url).mkString)
    
    val photoPrefix = (jsonResponse \ "response" \ "photos" \ "items" \ "prefix").extractOrElse[String]("")
    val photoSuffix = (jsonResponse \ "response" \ "photos" \ "items" \ "suffix").extractOrElse[String]("")
    
    Photo(photoPrefix, photoSuffix)
  }
  
  def writeVenueInfo(venue: Venue) = {
    val writer = new BufferedWriter(new FileWriter(dataFolder + venue.name.replace("/", "").replace(" ", "_") + ".txt"))
    
    writer.write(venue.name + "," + venue.id + "\n")
    
    val photo = getPhoto(venue.id)
    writer.write(photo.prefix + "," + photo.suffix + "\n")
    
    val tips = getTips(venue.id, 0)
    tips.foreach(tip => writer.write(tip.date + "|" + tip.text + "\n"))
    
    writer.close
  }
}