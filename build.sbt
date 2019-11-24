name := "Shyam_Patel_hw3"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  // Logback Classic Module
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // Typesafe Config
  "com.typesafe" % "config" % "1.4.0",
  // Spark Project Core
  "org.apache.spark" %% "spark-core" % "2.4.4",
  // ScalaTest
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  // SLF4J API Module
  "org.slf4j" % "slf4j-api" % "1.7.29"
)

// override default deduplicate merge strategy
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}

// show deprecation warnings
scalacOptions := Seq("-unchecked", "-deprecation")
