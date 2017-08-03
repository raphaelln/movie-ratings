package com.acme.ratings.job

import org.apache.log4j.Logger
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

/**
  * Calculate the insights and store at insights database, needs a ratings table registered as tempTable
  *
  * @param sqlContext
  * @param mongoHost
  * @param mongoPort
  */
class CalculateInsights(sqlContext: SQLContext, mongoHost:String, mongoPort:Int) {

  private val logger = Logger.getLogger(getClass.getName)

      def apply(): Unit = {
          bestMovies()
          bestGenres()
          moviesByYear()
          moviesByDecade()
          averageByYear()
          moviesByGenre()
    }

    private def bestMovies(): Unit = {

      logger.info(s"*********** bestMovies")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "bestMovies", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      //calculate ratings
      sqlContext.sql("select movie_id, avg(ratingValue) as average, count(movie_id) as numRatings from ratings group by movie_id").registerTempTable("movieRatings")
      //get best movies
      val moviesSQL = """ select distinct r.movie_id, r.title, r.year, mr.average, r.genres from ratings r
                        inner join movieRatings mr on r.movie_id = mr.movie_id
                        where mr.numRatings > 5
                        order by mr.average desc """
      val bestMovies  = sqlContext.sql(moviesSQL)
      bestMovies.withColumn("_id", col("movie_id")).drop("movie_id").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

    private def moviesByYear(): Unit = {
      logger.info(s"*********** moviesByYear")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "moviesByYear", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      val moviesByYear = sqlContext.sql("select count(distinct(movie_id)) as movies, year from ratings group by year order by movies desc")
      moviesByYear.withColumn("_id", col("year")).drop("year").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

    private def moviesByDecade(): Unit = {
      logger.info(s"*********** moviesByDecade")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "moviesByDecade", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      val moviesByYear = sqlContext.sql("select distinct count(distinct(movie_id)) as movies, floor(year/10) as decade from ratings group by floor(year/10) order by decade desc")
      moviesByYear.withColumn("_id", col("decade")).drop("decade").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

    private def averageByYear(): Unit = {
      logger.info(s"*********** averageByYear")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "yearRatings", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      //calculate year ratings
      val yearRatings = sqlContext.sql(" select m.year,avg(m.ratingValue)  as average from (select  year(timestamp(from_unixtime(ratingDate))) as year,ratingValue from ratings) as m group by year")
      yearRatings.withColumn("_id", col("year")).drop("year").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

    private def moviesByGenre(): Unit = {
      logger.info(s"*********** moviesByGenre")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "moviesByGenre", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      //calculate movies by genre
      val moviesByGenreDF = sqlContext.sql(" select g.genre,count(g.movie_id) as movies from (select distinct movie_id,explode(genres) as genre from ratings) as g group by g.genre order by movies desc")
      moviesByGenreDF.withColumn("_id", col("genre")).drop("genre").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

    private def bestGenres(): Unit = {
      logger.info(s"*********** bestGenres")
      val mongoConfigs = Map("host" -> s"$mongoHost:$mongoPort", "database" -> "insights","collection" -> "bestGenres", "idField" -> "id", "splitKey" -> "id", "splitSize" -> "10", "idAsObjectId" -> "true")
      //calculate movies by genre
      val averageByGenreDF = sqlContext.sql(" select g.genre,avg(g.ratingValue) as average from (select ratingValue, explode(genres) as genre from ratings) as g group by g.genre order by average desc")
      averageByGenreDF.withColumn("_id", col("genre")).drop("genre").write.format("com.stratio.datasource.mongodb").mode("append").options(mongoConfigs).save()
    }

}
