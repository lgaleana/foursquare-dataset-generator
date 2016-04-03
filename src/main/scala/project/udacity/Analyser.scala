package project.udacity

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties

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
  
}