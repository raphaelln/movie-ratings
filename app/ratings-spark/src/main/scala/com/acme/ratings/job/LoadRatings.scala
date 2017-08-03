package com.acme.ratings.job

import com.stratio.datasource.mongodb._
import com.stratio.datasource.mongodb.config.MongodbConfig._
import com.stratio.datasource.mongodb.config.MongodbConfigBuilder
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

/**
  * Responsable for loading the ratings from mongodb database
  * @param sqlContext
  */
class LoadRatings(sqlContext:SQLContext, mongoHost:String, mongoPort:Int) {

  def apply(): Unit = {
    loadDataFromMongo()
  }

  /**
    * Load data and register ratings dataframe
    */
  private def loadDataFromMongo(): Unit = {

    loadMoviesFromMongo()
    loadRatingsFromMongo()
    //rename _id columns
    sqlContext.sql("select * from moviesMongo").withColumn("movie_id", col("_id")).drop("_id").registerTempTable("moviesMongo")
    sqlContext.sql("select * from ratingsMongo").withColumn("rating_id", col("_id")).drop("_id").registerTempTable("ratingsMongo")
    sqlContext.sql("select m.movie_id, m.title, m.genres, m.year, r.ratingValue, int(r.ratingDate) as ratingDate from moviesMongo m inner join ratingsMongo r on r.movieId = m.movie_id ").registerTempTable("ratings")



  }

  private def loadMoviesFromMongo(): Unit = {

    val builder = MongodbConfigBuilder(Map(Host -> List(s"$mongoHost:$mongoPort"), Database -> "ratings", Collection ->"movies", SamplingRatio -> 1.0, WriteConcern -> "normal"))
    val readConfig = builder.build()
    val mongoRDD = sqlContext.fromMongoDB(readConfig)
    mongoRDD.registerTempTable("moviesMongo")
  }

  private def loadRatingsFromMongo(): Unit = {

    val builder = MongodbConfigBuilder(Map(Host -> List(s"$mongoHost:$mongoPort"), Database -> "ratings", Collection ->"ratings", SamplingRatio -> 1.0, WriteConcern -> "normal"))
    val readConfig = builder.build()
    val mongoRDD = sqlContext.fromMongoDB(readConfig)
    mongoRDD.registerTempTable("ratingsMongo")
  }

}
