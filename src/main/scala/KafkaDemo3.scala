package com.aagmon.demos


import com.aagmon.demos.Domain.SessionObj
import io.circe.generic.auto._
import org.apache.kafka.clients.consumer.ConsumerConfig

import java.util.Properties
//import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}



object KafkaDemo3 {

  import Implicits._

   def aggregateSession(key: String, value: String, currentSession: SessionObj): SessionObj = {

    value match {
      case x if x == "start" => currentSession.copy(sessionId = key, startTime = 1)
      case x if x == "stop" => currentSession.copy(sessionId = key, endTime = 1)
      case _ => currentSession
    }
  }


  def main(args: Array[String]): Unit = {
    println("starting [3]")

    val bootstrapServer:String = scala.util.Properties.envOrElse("KAFKA_SERVER", "127.0.0.1:9092")
    val conf = new Properties
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-demo-3")
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)

    // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
    conf.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


    val sessionObject = new SessionObj(sessionId = "", startTime = 0, endTime = 0)

    // start creating the topology
    val builder  = new StreamsBuilder
    val sessionEventsStream: KStream[String, String] = builder.stream[String, String]("session-events")

    sessionEventsStream
      .peek((key, value) => {
        println(f"STREAM IN => Key:${key} Value:${value}")
      })
      .groupByKey
      .aggregate(sessionObject)(aggregateSession)
      .toStream
      .filter((sessionID, currentSession) => {
        currentSession.startTime > 0 &&  currentSession.endTime > 0
      })
      .peek((key, value) => {
        println(f"Session => Key:${key} Value:${value}")
      })
      .to("sessions-complete")


    val topology: Topology = builder.build()
    val streams: KafkaStreams = new KafkaStreams(topology, conf)

    streams.cleanUp() // only for test
    streams.start()



    // shutdown hook to correctly close the streams application
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        streams.close()
      }
    })

  }

}
