package com.acme.ratings.model

/**
  * Created by rnascimento on 01/08/2017.
  */
case class Rating (movie_id:Int, title:String, year:Int, genres:Array[String], rating_id:Int, ratingValue:Int, feeling:String, ratingDate:String) extends java.io.Serializable