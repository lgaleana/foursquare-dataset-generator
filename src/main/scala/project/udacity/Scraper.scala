package project.udacity

import net.liftweb.json._
import java.io.BufferedWriter
import java.io.FileWriter

object Scraper {
  val CLIENT_ID = "QHOC11AI2RYV2J2XHA3CF4C31RULJS4HNQ0JZHL2XI3OKUN3"
  val CLIENT_SECRET = "SSIEUDJYYZVU2PMPSVX1SRP312TKRC2TGDGEFA5FC0KHIF4D"
  
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
  
  def main(args: Array[String]): Unit = {
    val venueNameId = categoryIds.flatMap(getVenueIds).toSet
    val writer = new BufferedWriter(new FileWriter(venueIdsFile))
    venueNameId.foreach(nameId => {
      writer.write(nameId.mkString(",") + "\n")
    })
    writer.close
    println("Total no. venues: " + venueNameId.size)
  }
  
  def getVenueIds(category: String) = {
    val url = s"https://api.foursquare.com/v2/venues/search?v=20160402&intent=browse&limit=50&sw=20.591652120829167%2C-103.4446907043457&ne=20.753866576560597%2C-103.23698043823242&categoryId=$category&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET"
    val jsonResponse = parse(scala.io.Source.fromURL(url).mkString)
    val venueNamesObj = jsonResponse \ "response" \ "venues" \ "name"
    val venueIdsObj = jsonResponse \ "response" \ "venues" \ "id"
    val venueNameId = (venueNamesObj.children zip venueIdsObj.children).map {
      case (JField(_, JString(name)), JField(_, JString(id))) => Seq(name, id)
      case _ => Seq()
    }
    println("No. venues: " +venueNameId.size)
    venueNameId
  }
}