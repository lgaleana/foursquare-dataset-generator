package project.udacity

import java.io.File
import scala.io.Source
import java.net.URLEncoder
import net.liftweb.json._
import java.io.FileWriter
import java.io.BufferedWriter

/**
 * @author lgaleana
 */
object Translator {
  
  val SERVER_KEY = "AIzaSyCcB0p6FYwqgm61dzJktdQSD-FbfeLv_NY"
  
  val dataFolder = "data/"
  val translatedDataFolder = "translated_data/"
  val lastFile = "last_translated.txt"
  
  case class TranslatedVenue(nameId: String, picture: String, text: String)
  
  def main(args: Array[String]): Unit = {
    val last = Source.fromFile(lastFile).getLines.toSeq.head.toInt
    
    val files = new File(dataFolder).listFiles.zipWithIndex
    val translatedTips = files.drop(last).map(translateTips)
    
    translatedTips.foreach(writeTranslatedText)
  }
  
  def translateTips(venueIndex: (File, Int)) = {
    implicit val formats = DefaultFormats
    
    val lines = Source.fromFile(venueIndex._1).getLines.toSeq
    
    val text = URLEncoder.encode(lines.drop(2).foldLeft("")((text, line) => {
      val tip = line.split('|')
      val text = URLEncoder.encode(tip(1), "UTF-8")
      
      val url = s"https://www.googleapis.com/language/translate/v2?target=en&key=$SERVER_KEY&q=$text"
      println(s"Requesting $url")
      val jsonResponse = parse(scala.io.Source.fromURL(url).mkString)
      
      tip(0) + "," + (jsonResponse \\ "translatedText").extract[String] + "\n"
    }), "UTF-8")
    
    val writer = new BufferedWriter(new FileWriter(lastFile))
    writer.write((venueIndex._2 + 1).toString)
    writer.close
    
    if(!text.isEmpty)
      TranslatedVenue(lines(0), lines(1), text)
    else
      TranslatedVenue(lines(0), lines(1), "")
  }
  
  def writeTranslatedText(venue: TranslatedVenue) = {
    val name = venue.nameId.split(',')(1)
    val writer = new BufferedWriter(new FileWriter(translatedDataFolder + name.replace("/", "").replace(" ", "_")))
    writer.write(venue.nameId + "\n")
    writer.write(venue.picture + "\n")
    writer.write(venue.text + "\n")
    writer.close
  }
  
}