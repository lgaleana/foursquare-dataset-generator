name := """foursquare-scraper"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.2.4" % "test",
	"net.liftweb" %% "lift-json" % "2.6+",
	"edu.stanford.nlp" % "stanford-corenlp" % "3.5.2",
  	"edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" classifier "models"
)

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

