package com.aagmon.demos

import com.aagmon.demos.Domain.SessionObj
import io.circe.generic.auto._
import org.apache.kafka.clients.consumer.ConsumerConfig
//import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

import java.util.Properties

object KafkaDemo2 {

  import Implicits._

  def main(args: Array[String]): Unit = {
    println("starting [2]")
    val conf = new Properties
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-demo-2")
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
    conf.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val builder  = new StreamsBuilder

    val usersColorsStream: KStream[String, String] = builder.stream[String, String]("users-colors")

    usersColorsStream
      .mapValues{ value => new SessionObj(value, 1, 2)}
      .to("users-objects")



    // just a test
    //val streamTable:KTable[String, String] = usersColorsStream.toTable
    // streamTable.toStream.print(Printed.toSysOut)
    //


    // run this to clean up /Users/aagmon/Downloads/kafka_2.13-3.1.0/bin/kafka-streams-application-reset.sh --application-id "kafka-demo-2" --bootstrap-servers "127.0.0.1:9092" --input-topics "users-colors"

    val topology: Topology = builder.build()
    val streams: KafkaStreams = new KafkaStreams(topology, conf)


    streams.cleanUp() // only for test
    streams.start()

    // print the topology
    //streams.metadataForLocalThreads().forEach(t => System.out.print(t.toString))

    // shutdown hook to correctly close the streams application
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        streams.close()
      }
    })
  }

}
