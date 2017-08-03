#!bin/bash

cd /app/ratings-spark
sbt package
cd /app/ratings-spring
mvn clean install
