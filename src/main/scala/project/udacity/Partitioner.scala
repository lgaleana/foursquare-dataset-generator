package project.udacity

import java.io.File
import scala.io.Source
import java.io.FileWriter
import java.io.BufferedWriter

/**
 * @author lgaleana
 */
object Partitioner {
  
  val dictionary = Source.fromFile("dictionary").getLines.toSet.map((word: String) => word.toLowerCase)
  
  val analysedDataFolder = "analysed_data/"
  val filteredDataFolder = "partitioned_data/"
  val existentFile = "existent_attributes.txt"
  val nonExistentFile = "non_existent_attributes.txt"
  
  case class Attribute(name: String, frequency: Int)
  case class PartitionedAttributes(existent: Seq[Attribute], nonExistent: Seq[Attribute])
  case class PartitionedMaps(existent: Map[String, Int], nonExistent: Map[String, Int])
  
  def main(args: Array[String]): Unit = {
    val partitionedAttributes = new File(analysedDataFolder).listFiles.map(partitionAttributes).toSeq
    val partitionedMaps = mergePartitions(partitionedAttributes)
    val existentAttributes = partitionedMaps.existent.toSeq.filter(attribute => {
      attribute._1.length() > 1 && attribute._2 >= 10
    }).sortBy(- _._2)
    val nonExistentAttributes = partitionedMaps.nonExistent.toSeq.filter(attribute => {
      attribute._1.length() > 1 && attribute._2 >= 10
    }).sortBy(- _._2)
    
    val existentWriter = new BufferedWriter(new FileWriter(filteredDataFolder + existentFile))
    existentAttributes.foreach(attribute => existentWriter.write(attribute._1 + "," + attribute._2 + "\n"))
    existentWriter.close
    
    val nonExistentWriter = new BufferedWriter(new FileWriter(filteredDataFolder + nonExistentFile))
    nonExistentAttributes.foreach(attribute => nonExistentWriter.write(attribute._1 + "\n"))
    nonExistentWriter.close
  }
  
  def partitionAttributes(venueFile: File) = {
    val lines = Source.fromFile(venueFile).getLines.drop(2)
    val attributes = lines.map(line => {
      val attributeInfo = line.split(',')
      attributeInfo(0) -> attributeInfo(1).toInt
    }).toMap
    attributes.partition(attribute => dictionary.contains(attribute._1.toLowerCase))
  }
  
  def mergePartitions(partitioned: Seq[(Map[String, Int], Map[String, Int])]) = {
    partitioned.foldLeft(PartitionedMaps(Map.empty[String, Int], Map.empty[String, Int]))((mergedPartitions, parts) => {
      PartitionedMaps(mergedPartitions.existent ++ parts._1.map {
        case (token, freq) => token -> (freq + mergedPartitions.existent.getOrElse(token, 0))
      },
      mergedPartitions.nonExistent ++ parts._2.map {
        case (token, freq) => token -> (freq + mergedPartitions.nonExistent.getOrElse(token, 0))
      })
    })
  }
  
}