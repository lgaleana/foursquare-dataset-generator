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
  
  def main(args: Array[String]): Unit = {
    
  }
  
  def analyseVenue(venueIndex: (File, Int)) = {
   Source.fromFile(venueIndex._1).getLines.drop(2).foreach(tip => {
     val tipText = tip.split('|')(1)
     
     val annotatedTip = new Annotation(tipText);
     pipeline.annotate(annotatedTip);
     val sentences = annotatedTip.get(classOf[SentencesAnnotation]).asScala
   })
  }
  
  def rateSentences(sentences: Seq[CoreMap]) = {
    for {
      sentence    ← sentences
      tree        = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      sentiment   = Sentiment(RNNCoreAnnotations.getPredictedClass(tree))
      tokens      = getTokens(sentence.get(classOf[TokensAnnotation]).asScala)
    } yield buildTokensMap(tokens, sentiment)
  }
  
  def getTokens(tokens: Seq[CoreLabel]) =
    for {
      token ← tokens
      lemma = token.get(classOf[LemmaAnnotation])
      pos   = token.get(classOf[PartOfSpeechAnnotation])
      if pos.equals("NN") || pos.equals("NNS") || pos.equals("JJ")
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
}

object Sentiment {
  val VeryPositive = 1
  val Positive     = 1
  val Neutral      = 1
  val Negative     = 0
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