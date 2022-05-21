package com.aagmon.demos

import com.aagmon.demos.DomainObjects.Session
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer, StringSerializer}
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.scala.serialization.Serdes

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

//sbt compile will generate the protos

object KafkaDemo4 {

  val sessionSerde: Serde[Session] = Serdes.fromFn(
    //serializer
    (session:Session) => session.toByteArray,
    // deserializer
    (sessionBytes:Array[Byte]) => Option(Session.parseFrom(sessionBytes))
  )


  def getDefaultProperties: Properties = {
    val bootstrapServer:String = scala.util.Properties.envOrElse("KAFKA_SERVER", "127.0.0.1:9092")
    val conf = new Properties
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-demo-4")
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
    conf.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    conf
  }

  def produceMessages(numMessages:Int, topic:String): Unit = {
    val props = getDefaultProperties
    val producer = new KafkaProducer[String, Session](props, Serdes.stringSerde.serializer(), sessionSerde.serializer())

    for (message_id <- 0 to numMessages) {

      print(s"Sending message $message_id\n")

      val session:Session = new Session()
        .withId(message_id.toString)
        .withStartTime(message_id * 3)
        .withEndTime(message_id * 5)

      producer.send(new ProducerRecord[String, Session](topic, message_id.toString, session))
    }
    producer.close()
  }


  def streamProtoMessages(topic:String): Unit = {

  }


  def main(args: Array[String]): Unit = {
    produceMessages(100, "proto-test-topic-sessions")
  }

}
