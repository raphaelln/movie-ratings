
version := "1.0"

scalaVersion := "2.10.6"


lazy val artifactSettings = Seq(
  organization := "com.company.ratings",
  name := "ratings-spark"
)

lazy val sparkVersion = "1.6.0"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-streaming_2.10" % "1.6.0",
  "com.stratio.datasource" % "spark-mongodb_2.10" % "0.11.2",
  "org.apache.spark" %% "spark-streaming-flume" % "1.6.0",
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "log4j" % "log4j" % "1.2.14"
)
        