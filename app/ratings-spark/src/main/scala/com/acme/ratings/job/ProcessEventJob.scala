package com.acme.ratings.job

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.functions.col

/**
  * Created by rnascimento on 03/08/2017.
  */
class ProcessEventJob(sqlContext: SQLContext, mongoHost:String, mongoPort:Int) extends java.io.Serializable {

  object Holder extends Serializable {
    @transient lazy val logger = Logger.getLogger(getClass.getName)
  }


  def process(eventsDF:DataFrame): Unit ={

    //register events temp table
    eventsDF.registerTempTable("events")
    //load data from mongodb
    new LoadRatings(sqlContext, mongoHost, mongoPort)()
    mergeMovies()
    mergeMovieGenres()
    mergeRatings
    new CalculateInsights(sqlContext, mongoHost, mongoPort)()

  }

  private def mergeMovies(): Unit = {

    Holder.logger.info(s"*********** Merging movies")
    sqlContext.sql("select key_value as movieId, fields[0] as title, int(fields[1]) as year from events  where target='MOVIES'").registerTempTable("movieEvents")
    val count = sqlContext.sql("select movieId from movieEvents").count()
    if (count > 0) {
      val mergedMovies =  sqlContext.sql("select e.movieId, e.title, e.year, m.genres from movieEvents e left join  moviesMongo m on e.movieId = m.movie_id")
      mergedMovies.withColumn("_id", col("movieId")).drop("movie_id").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "movies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()
    }
  }


  private def safeGet (ar:Seq[String]): Seq[String] = {
    val newArray =  ar match {
      case array:Seq[String] => array
      case _ => Seq[String]()
    }
    newArray
  }

  private def mergeMovieGenres(): Unit = {
    Holder.logger.info(s"*********** Merging Genres")
    sqlContext.udf.register("concat_array", (ar1:Seq[String], ar2:Seq[String]) => {
      (safeGet(ar1) ++ safeGet(ar2)).toList.distinct.sorted

    })
    sqlContext.sql("select key_value as movieId, collect_list(fields[0]) as genres from events where target='MOVIE_GENRE' group by key_value").registerTempTable("genreEvents")
    val count = sqlContext.sql("select movieId from genreEvents").count()
    if (count > 0) {
      val mergedGenres =  sqlContext.sql("select e.movieId, m.title, m.year, concat_array(m.genres, e.genres) as genres from genreEvents e left join moviesMongo m on e.movieId = m.movie_id")
      mergedGenres.withColumn("_id", col("movieId")).drop("movieId").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "movies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()
    }

  }

  private def mergeRatings(): Unit = {
    Holder.logger.info(s"*********** Merging Ratings")
    sqlContext.sql("select key_value as ratingId, int(fields[0]) as movie_id, int(fields[1]) as ratingValue, fields[2] as ratingDate from events where target='MOVIE_RATINGS'").registerTempTable("ratingEvents")

    val count = sqlContext.sql("select ratingId from ratingEvents").count()
    if (count > 0) {
      val mergedRatings =  sqlContext.sql("select e.ratingId, e.movie_id as movieId, e.ratingValue, timestamp(from_unixtime(e.ratingDate)) as ratingDate from ratingEvents e left join ratingsMongo r on e.ratingId = r.rating_id")
      print("merged Ratings " + mergedRatings.count())
      mergedRatings.withColumn("_id", col("ratingId")).drop("ratingId").write.format("com.stratio.datasource.mongodb").mode("append").options(Map("host" -> s"$mongoHost:$mongoPort", "database" -> "ratings","collection" -> "ratings", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")).save()

    }
  }

}
