package project.udacity

import scala.io.Source
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

/**
 * @author lgaleana
 */
object Filterer {
  
  val attributes = Source.fromFile("attributes_2.txt").getLines.map((word: String) => word.toLowerCase -> 0).toMap
  
  val analysedDataFolder = "analysed_data/"
  val data = "test_data.csv"
  
  case class Venue(name: String, attributes: Map[String, Int])
  
  def main(args: Array[String]): Unit = {
    val venues = new File(analysedDataFolder).listFiles
    val filteredVenues = venues.map(filterAttributes).toSeq
    writeAttributes(filteredVenues)
  }
  
  def filterAttributes(venueFile: File) = {
    val lines = Source.fromFile(venueFile).getLines.toSeq
    Venue(lines.head.split(',')(0), attributes ++ (lines.drop(2).map(line => {
      val attributeInfo = line.split(',')
      attributeInfo(0) -> attributeInfo(1).toInt
    }).filter(attribute => attributes.contains(attribute._1)).toMap))
  }
  
  def filterVenues(venue: Venue) =
    venue.attributes.map(_._2).sum > 0
  
  def writeAttributes(venues: Seq[Venue]) = {
    val writer = new BufferedWriter(new FileWriter(data))
    writer.write(venues.head.attributes.map(attribute => attribute._1).mkString(",") + "\n")
    venues.foreach(venue => {
      writer.write(venue.name + "," + venue.attributes.map(attribute => attribute._2).mkString(","))
      writer.write("\n")
    })
    writer.close
  }

}