package com.acme.ratings.job

import com.acme.ratings.model.Rating
import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by rnascimento on 01/08/2017.
  */
object IngestRatings extends java.io.Serializable {

  val logger = Logger.getLogger(IngestRatings.getClass.getName)

  private lazy val sparkContext = {
      val conf = new SparkConf()
      new SparkContext(conf)
    }

    private lazy val sqlContext = {
      val sqlContext = new HiveContext(sparkContext)
      sqlContext
    }

    private var dbPath:String  = _
    private var mongoHost:String  = _
    private var mongoPort:Int  = _



    def apply(dbPath: String, mongoHost:String, mongoPort:Int): Unit = {
      this.dbPath = dbPath
      this.mongoHost = mongoHost
      this.mongoPort = mongoPort
      launch()
    }

    private def launch(): Unit = {


      readData()
      //ingest data on the ratings database
      ingestMovies()
      ingestRatings()
      // calculate insights to insights database
      new CalculateInsights(sqlContext,mongoHost, mongoPort)()
    }

    def readData(): Unit = {
      import sqlContext.implicits._
      val ratingsDF = sparkContext.textFile(dbPath).map(rec => rec.split('|')).map(rec => Rating(rec(0).toInt, rec(1), rec(2).toInt, rec(3).split(","), rec(4).toInt, rec(5).toInt, rec(6), rec(7))).toDF()
      ratingsDF.registerTempTable("ratings")
    }

    def ingestMovies(): Unit = {
      val moviesDF = sqlContext.sql("select distinct movie_id, title, year, genres from ratings")
      moviesDF.withColumn("_id", col("movie_id")).drop("movie_id").
        write.
        format("com.stratio.datasource.mongodb").mode("append").
        options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "movies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).
        save()
    }

    def ingestRatings(): Unit = {
      val finalRatings = sqlContext.sql(""" select distinct rating_id,  movie_id as movieId, ratingValue, feeling,  timestamp(from_unixtime(ratingDate)) as ratingDate from ratings """)
      finalRatings.withColumn("_id", col("rating_id")).drop("rating_id").
        write.
        format("com.stratio.datasource.mongodb").mode("append").
        options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "ratings", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true"))
        .save()
    }

}
