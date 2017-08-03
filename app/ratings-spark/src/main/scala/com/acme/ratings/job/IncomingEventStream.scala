package com.acme.ratings.job

import com.acme.ratings.model.Event
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions._
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._


/**
  * Class responsable for streaming flume events with changes on sqlite database
  * @param mongoHost
  * @param mongoPort
  */
class IncomingEventStream(mongoHost:String, mongoPort:Int) extends java.io.Serializable {

  object Holder extends Serializable {
    @transient lazy val logger = Logger.getLogger(getClass.getName)
  }

  lazy val streamContext = {
      val batchInterval = Milliseconds(20000)
      val sparkConf = new SparkConf().setAppName("Ratings Streaming")
      val ssc = new StreamingContext(sparkConf, batchInterval)
      ssc
    }

    lazy val sqlContext = {
      val sql =  new HiveContext(streamContext.sparkContext)
      sql
    }


    def apply(): Unit = {
      startStream()
    }




    def startStream(): Unit = {
      import org.apache.spark.streaming.flume._

      val stream = FlumeUtils.createStream(streamContext, "spark", 42222, StorageLevel.MEMORY_ONLY_SER_2)
      val events = stream.map( e => new String(e.event.getBody().array()).replaceAll("\"", ""))
                         .map(line =>line.split('|'))
                         .map(evt => Event(evt(0).toInt, evt(1), evt(2), evt(3).toInt, evt(4).split(',')))

      events.foreachRDD( e => {
          import sqlContext.implicits._
          val events = e.toDF()
          val numEvents = e.count()

          if (numEvents > 0) {
            Holder.logger.info("****************PROCESSING INCOMING EVENT*************************")
            new ProcessEventJob(sqlContext, mongoHost, mongoPort).process(events)
            Holder.logger.info("****************FINISHED INCOMING EVENT PROCESS*******************")
          } else {
            Holder.logger.info("**************** NO EVENTS RECEIVED, NOTHING TO DO.")
          }
      })

      streamContext.start()
      streamContext.awaitTermination()
   }
}
