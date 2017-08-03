package com.acme.ratings

import com.acme.ratings.job.IngestRatings

/**
  * Created by rnascimento on 01/08/2017.
  */
object IngestStartUp extends App {

  validateRequiredArguments()
  IngestRatings(args(0), args(1), args(2).toInt)

  def validateRequiredArguments() {

    if (Option(args).isEmpty || args.length < 3) {
      throw new IllegalArgumentException("Must pass arguments in the following order: dbPath, mongoHost,port.")
    }
  }

}
