ThisBuild / version := "0.7"
ThisBuild / scalaVersion := "2.13.8"

// build using ```sbt docker:publishLocal``` result should be in local

//lazy val root = (project in file("."))
//  .settings(
    name := "KafkaDemo3"
    assembly / mainClass := Some("com.aagmon.demos.KafkaDemo3")
    Compile / mainClass  := Some("com.aagmon.demos.KafkaDemo3")
    Docker / packageName   := "kafka-demo-3"

//  )

// for protofobuf
Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-streams" % "3.1.0",
  "org.apache.kafka" % "kafka-clients" % "3.1.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "3.1.0",

  // https://mvnrepository.com/artifact/io.circe/circe-core
   "io.circe" %% "circe-core" % "0.15.0-M1",
   "io.circe" %% "circe-generic" % "0.15.0-M1",
   "io.circe" %% "circe-parser" % "0.15.0-M1",

  // logging
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",


)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)



