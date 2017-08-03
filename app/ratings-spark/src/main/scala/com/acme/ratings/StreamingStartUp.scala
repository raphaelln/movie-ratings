package com.acme.ratings

import com.acme.ratings.job.RatingsStreaming

/**
  * Created by rnascimento on 01/08/2017.
  */
object StreamingStartUp extends App {

  validateRequiredArguments()
  new RatingsStreaming(args(0), args(1).toInt)()

  def validateRequiredArguments() {

    if (Option(args).isEmpty || args.length < 2) {
      throw new IllegalArgumentException("Must pass arguments in the following order: mongoHost , port.")
    }
  }

}
