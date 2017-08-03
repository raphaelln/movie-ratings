package com.acme.ratings.model

/**
  * Created by rnascimento on 01/08/2017.
  */
case class Event(id:Int, target:String, operation:String, key_value:Int, fields:Array[String]) extends java.io.Serializable
