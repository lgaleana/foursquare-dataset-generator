package project.udacity

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import java.io.File
import scala.io.Source
import edu.stanford.nlp.pipeline.Annotation
import scala.collection.JavaConverters._
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation
import java.io.FileWriter
import java.io.BufferedWriter

case class Token(text: String, sentiment: Int)

/**
 * @author lgaleana
 */
object Analyser {

  val annotators = "tokenize, ssplit, pos, lemma, parse, sentiment"
  val boundaryTokenRegex = "(?i)\\.|[!?]+|(?:but)|(?:yet)|(?:nevertheless)|(?:nonetheless)|(?:however)|(?:notwithstanding)|(?:although)|(?:though)|(?:whereas)"
  val tokenPatternsToDiscard = "(?i)(?:but)|(?:yet)|(?:nevertheless)|(?:nonetheless)|(?:however)|(?:notwithstanding)|(?:although)|(?:though)|(?:whereas)"

  val props = new Properties
  props.setProperty("annotators", annotators)
  props.setProperty("ssplit.boundaryTokenRegex", boundaryTokenRegex)
  props.setProperty("ssplit.tokenPatternsToDiscard", tokenPatternsToDiscard)
  val pipeline = new StanfordCoreNLP(props)
  
  val translatedDataFolder = "translated_data/"
  val analysedDataFolder = "analysed_data/"
  val lastFile = "last_analysed.txt"

  def main(args: Array[String]): Unit = {
    val last = Source.fromFile(lastFile).getLines.toSeq.head.toInt
    
    val venueFiles = new File(translatedDataFolder).listFiles.zipWithIndex
    venueFiles.drop(last).foreach(analyseVenue)
  }

  def analyseVenue(venueNameIndex: (File, Int)) = {
    println("Analysing " +  venueNameIndex._1.getName)
    val lines = Source.fromFile(venueNameIndex._1).getLines.toSeq
    val ratedTokens = lines.drop(2).flatMap(tip => {
      if(!tip.isEmpty) {
        val tipText = tip.split('|')(1)
  
        val annotatedTip = new Annotation(tipText);
        pipeline.annotate(annotatedTip);
        val sentences = annotatedTip.get(classOf[SentencesAnnotation]).asScala
  
        rateSentences(sentences)
      }
      else
        Seq()
    })
    
    writeTokens(mergeTokens(ratedTokens), lines(0), lines(1))
    
    val writer = new BufferedWriter(new FileWriter(lastFile))
    writer.write((venueNameIndex._2 + 1).toString)
    writer.close
  }

  def rateSentences(sentences: Seq[CoreMap]) = {
    for {
      sentence ← sentences
      tree = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      sentiment = Sentiment(RNNCoreAnnotations.getPredictedClass(tree))
      tokens = getTokens(sentence.get(classOf[TokensAnnotation]).asScala)
    } yield buildTokensMap(tokens, sentiment)
  }

  def getTokens(tokens: Seq[CoreLabel]) =
    for {
      token ← tokens
      lemma = token.get(classOf[LemmaAnnotation])
      pos = token.get(classOf[PartOfSpeechAnnotation])
      if lemma.matches("[a-zA-Z]+") && (pos.equals("NN") || pos.equals("NNS") || pos.equals("JJ"))
    } yield lemma

  def buildTokensMap(tokens: Seq[String], frequency: Int) =
    tokens.map(token => token -> frequency).toMap

  def mergeTokens(tokens: Seq[Map[String, Int]]) = {
    tokens.foldLeft(Map.empty[String, Int])((mergedTokens, tokensMap) => {
      mergedTokens ++ tokensMap.map {
        case (token, freq) => token -> (freq + mergedTokens.getOrElse(token, 0))
      }
    })
  }

  def writeTokens(tokens: Map[String, Int], venueNameId: String, pictures: String) = {
    val name = venueNameId.split(',')(0).replace("/", "").replace(" ", "_")
    val writer = new BufferedWriter(new FileWriter(analysedDataFolder + name + ".txt"))
    writer.write(venueNameId + "\n")
    writer.write(pictures + "\n")
    tokens.foreach(token => writer.write(token._1 + "," + token._2 + "\n"))
    writer.close
  }
}

object Sentiment {
  val VeryPositive = 1
  val Positive = 1
  val Neutral = 1
  val Negative = 0
  val VeryNegative = 0

  def apply(value: Int) =
    value match {
      case 0 => VeryNegative
      case 1 => Negative
      case 2 => Neutral
      case 3 => Positive
      case 4 => VeryPositive
    }
}