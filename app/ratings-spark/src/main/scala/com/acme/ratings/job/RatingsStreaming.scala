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
class RatingsStreaming(mongoHost:String, mongoPort:Int) {

  private val logger = Logger.getLogger(getClass.getName)

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

    private def mergeMovies(): Unit = {

      logger.info(s"*********** Merging movies")
      sqlContext.sql("select id as movieId, fields[0] as title, int(fields[1]) as year from events  where target='MOVIES'").registerTempTable("movieEvents")
      val mergedMovies =  sqlContext.sql("select m.movie_id, e.title, e.year, m.genres from moviesMongo m inner join movieEvents e on e.movieId = m.movie_id")
      mergedMovies.withColumn("_id", col("movie_id")).drop("movie_id").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "movies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()
    }

    private def mergeMovieGenres(): Unit = {
      logger.info(s"*********** Merging Genres")
      sqlContext.udf.register("concat_array", (ar1:Seq[String], ar2:Seq[String]) => (ar1 ++ ar2).toList.distinct.sorted)
      sqlContext.sql("select id as movieId, collect_list(fields[0]) as genres from events where target='MOVIE_GENRE' group by id").registerTempTable("genreEvents")
      val mergedGenres =  sqlContext.sql("select m.movie_id, m.title, m.year, concat_array(m.genres, e.genres) as genres from moviesMongo m inner join genreEvents e on e.movieId = m.movie_id")
      mergedGenres.withColumn("_id", col("movie_id")).drop("movie_id").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "movies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()

    }

    private def mergeRatings(): Unit = {
      logger.info(s"*********** Merging Ratings")
      sqlContext.sql("select id as ratingId, int(fields[0]) as movie_id, int(fields[1]) as ratingValue, fields[2] as ratingDate from events where target='MOVIE_RATINGS'").registerTempTable("ratingEvents")
      val mergedRatings =  sqlContext.sql("select r.rating_id, e.movie_id, e.ratingValue, timestamp(from_unixtime(e.ratingDate)) as ratingDate from ratingsMongo r inner join ratingEvents e on e.ratingId = r.rating_id")
      mergedRatings.withColumn("_id", col("rating_id")).drop("rating_id").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "ratings", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()
    }


    def startStream(): Unit = {
      import org.apache.spark.streaming.flume._

      val stream = FlumeUtils.createStream(streamContext, "spark", 42222, StorageLevel.MEMORY_ONLY_SER_2)
      val events = stream.map( e => new String(e.event.getBody().array()).replaceAll("\"", ""))
                         .map(line => line.split('|'))
                         .map(evt => Event(evt(0).toInt, evt(1), evt(2), evt(3).toInt, evt(4).split(',')))


      val numEvents = events.count()
      logger.info(s"*************************** Received events from flume $numEvents. Starting processing ***************************")
      events.foreachRDD( e => {
          import sqlContext.implicits._
          val df = e.toDF()

          val numEvents = e.count()

          if (numEvents > 0) {
            logger.info("*****************************************")
            df.show()
            logger.info("****************EVENTS CONTADOR"  + df.count())


            df.printSchema()
            logger.info("*****************************************")

            df.registerTempTable("events")
            //read data from mongodb
            new LoadRatings(sqlContext, mongoHost, mongoPort)()
            mergeMovies()
            mergeMovieGenres()
            mergeRatings()
            //runs the insights calculator
            new CalculateInsights(sqlContext, mongoHost, mongoPort)()
          } else {
            logger.info("**************** NO EVENTS RECEIVED, NOTHING TO DO.")
          }


      })

      streamContext.start()
      streamContext.awaitTermination()
   }
}
