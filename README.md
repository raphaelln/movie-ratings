Movie Insights
====================

Scenario
--------------------

The company acme corporation wants to build a solution to collect statistics in real time from movies ratings.

Those statistics are stored in a relational database.

Beside the fact the database today is small, acme corporations really care about of the database's growing size and expects the performance of calculation of the statistics won't be affected.

During the analysis the technical team discover that database is very slow and calculate the statics using that database will not be possible.

Problem Statement 
----------------------

As a software engineer at acme corporation build a solution regarding the following requirements:

 * Create a new database (analytics) to store the ratings and calculate the insights using it.
* The database must be a NoSQL database.
* The database must be an open source database.
* A new record on ratings or movies on the relational database must be automatically send to the analytics database.
* The analytics database must be build to answer the following questions:
    * Which movies has the best average ratings ?
    * Which genres has the best average ratings ?
    * Which genres has more movies ?
    * What is the average rating by genre ?
    * What is the average rating by year ?
    * How many movies are distributed by year ?
    * How many movies are distributed by decade ?
* The result for these questions must be provided by REST endpoints, for example:

```
[
{
   id: “5513770”,
      title: “Kung Fu Panda: Secrets of the Scroll (2016)”,
      year: “2016”,
      avg_rating: “6.9”,
      genres: [ “Short”, “Action”, “Animation”]
           },
   ...
]
```
  

Following the ER diagram of the database:

![](/assets/database.png)


Solution
====================

The resolution of problem was divided in three phases:
  
  * Ingest: Ingestion of the entire database to a analytics database;
  * Streaming Pipeline: Streaming pipeline to ingest real time data to analytics database.
  * Rest Client: Rest endpoints with the previous insights answered.
  
  Thinking on big data, and the growing of the data, two approaches was developed:
      1. Ratings: Only contains the data ingested from the relational database and the data from the streaming pipeline. All the insights will be computed on the client through mongodb queries.
      2: Insights: Wil compute the insights  on the spark and the results will be stored on the insights database. The client only will show the results to the final client.
        

Ingest
---------------

### Ingest Data from the database

The initial intention was use **sqoop** to ingest all data from movies database to hdfs. But I have faced some problems, one of them made is impossible to use the sqoop.

After some *googlings* I have founded the following ticket on the sqoop issue tracker: :bomb: **https://issues.apache.org/jira/browse/SQOOP-2415**

Fortunately the sqlite has a small tool called *sqlite3*, this is a shell client which provide functionality to manage SQLite databases, one of them include the possibility to query the database and store the result on a file.

So for the ingest the following command as used:

```
sqlite3 -noheader -separator "|" /db/twitter-movie-ratings.db "select m.id, m.title, m.year, GROUP_CONCAT(distinct(g.title)) as genres, mv.id as rating_id, r.id as rating_value, r.feeling, mv.timestamp as ratingDate  from movies m left join movie_genre mg on mg.movie_id = m.id left join genres g on g.id = mg.genre_id inner join movie_ratings mv on mv.movie_id = m.id inner join ratings r on r.id = mv.rating_id group by m.id, m.title,m.year,rating_id,rating_value, r.feeling, ratingDate " > /db/ratings.in
```

The main idea was store the file in the hdfs, by using sqoop, but due the incompatibility, a file was generated and the following command is necessary to ingest data in the hdfs:
```
   hdfs dfs -mkdir -p /user/spark/ratings/ 
   hdfs dfs -put /db/ratings.in /user/spark/ratings/ 
```

### Processing and store

For processing and store the data a simple job spark as built.

This job has the following responsibilities:

1. Load ingest file
2. Create 2 collections on the ratings database
    2.1. Movies: Collection with data from movies and genres.
    2.2. Ratings: Collection with data from ratings.
3. Compute the statistics and store in the insights database.    
    
  
Streaming Pipeline
---------------  

After the ingestion phase, we need to continuous streaming data from the relational database to the **ratings** and **insights** database.

The following picture shows us how the streaming pipeline was architect:

![](/assets/ratings.png)

The above diagram show us the following components:

* Flume: Flume agent is responsible to continuous execute queries on the relational for new incoming events stored in a table called **Events**, pipe these events on the channel and sink in an avro sink on a specific host and port.
* Spark streaming: Create a stream on the host and specific port, which will continuous receive data from flume, merge the incoming data with the **ratings** database and compute all the statistics and store in the **insights** database.
      
### Changes on the database

Due the lack of support of broker technologies from the sqlite database, some changes was made on the relational database:
* Created a table called **events** to store the events on the tables;
* Created a insert trigger on the movies table;  
 * Created a insert trigger on the movie_ratings table;
* Created a insert trigger on the movie_genres table;

:blue_book: **The solution are not consider update, delete events. Only insert events are considered in this example.** 


Rest Client
---------------

Two rest applications was developed, using spring boot technologies.
  1. Will responsible to query the data on the **ratings** database and compute the insights on the fly.
  2. Will responsible to query the data on the **insights** database and show the previous computed insights.


Running
==================== 

###  Requirements

* Docker 
 
### Technology Summary

Some of technologies used:

* spark 1.6.0
* scala 2.10.6
* hadoop 2.6.0
* sbt 0.13.16
* flume 1.7.0
* sqlite-jdbc-3.19.3
* sqlite dialect
* jdk 1.8
* mongodb 3.0
* maven 3.5.0
* spark-streaming-flume
* spark-mongodb_2.11.011.1
* flume-ng-sql-source 1.4.4
    
   :warning: I have cloned the repository and made a small change in code because the custom source grab the connection, these was blocking the entire database and the following error was occurred:
   
   ```
   Database is locked
   ```

So, when flume is alive, no more connections with the relational database could be made, and new inserts on the movies table as impossible to be made. A small change was made, to avoid the flume to keep with the connection after the queries the database resolve this problem.
    
###  Build

```
docker-compose build
```

Now, grab a :coffee:, relax and wait to build all the images, this may take a while :zzz:.

### Run

```
docker-compose up
```

### Ingest

With all containers up, run:

```
docker exec -it ratings hadoop dfsadmin -safemode leave
docker exec -it dockercompose_spark_1 hdfs dfs -mkdir -p /user/spark/ratings/ingest
docker exec -it dockercompose_spark_1 hdfs dfs -put /db/ratings.in /user/spark/ratings/ingest
docker exec -it dockercompose_spark_1 spark-submit --packages org.apache.spark:spark-streaming-flume_2.10:1.6.0,com.stratio.datasource:spark-mongodb_2.10:0.11.2 --class com.acme.ratings.IngestStartUp  /app/ratings-spark/target/scala-2.10/ratings-spark_2.10-1.0.jar  /user/spark/ratings/ingest/ratings.in mongo 27017
```

### Streaming

With all containers up and ingest phase done, run:

```
docker exec -it dockercompose_spark_1 spark-submit --packages org.apache.spark:spark-streaming-flume_2.10:1.6.0,com.stratio.datasource:spark-mongodb_2.10:0.11.2 --class com.acme.ratings.StreamingStartUp  /app/ratings-spark/target/scala-2.10/ratings-spark_2.10-1.0.jar mongo 27017
docker exec -it dockercompose_flume_1 -n ratings -c conf -f flume.conf  -Dflume.root.logger=INFO,console -Dflume.monitoring.type=http -Dflume.monitoring.port=34545
```

### Rest Endpoints

#### Ratings

| Requirement        | Endpoint           |
| ------------- |:-------------:|
|  Which movies has the best average ratings ? | http://localhost:8080//movies/best/{top} or /movies/best/ |
|  Which genres has the best average ratings ? | http://localhost:8080//genres/best/{top} |
|  Which genres has more movies ? |  http://localhost:8080//genres/by/movies |
|  What is the average rating by genre ? | http://localhost:8080//genres/best/ |
|  What is the average rating by year ? |  http://localhost:8080//movies/year |
|  How many movies are distributed by year ? |  http://localhost:8080//movies/by/year |
|  How many movies are distributed by decade ? |  http://localhost:8080//movies/by/decade |

#### Insights

| Requirement        | Endpoint           |
| ------------- |:-------------:|
|  Which movies has the best average ratings ? | http://localhost:8081//insights/movies/best/{top} or /insights/movies/best |
|  Which genres has the best average ratings ? | http://localhost:8081//insights/genres/best/{top} |
|  Which genres has more movies ? |  http://localhost:8081//insights/genres/by/movies |
|  What is the average rating by genre ? | http://localhost:8081//insights/genres/best/ |
|  What is the average rating by year ? |  http://localhost:8081//insights/movies/year |
|  How many movies are distributed by year ? |  http://localhost:8081//insights/movies/by/year |
|  How many movies are distributed by decade ? |  http://localhost:8081//insights/movies/by/decade |


References and Libraries
====================


* Docker Images:
  * https://github.com/sequenceiq/docker-spark
  * https://github.com/docker-library/mongo
  * 
* Libraries:
  * https://github.com/Stratio/Spark-MongoDB
  * https://github.com/keedio/flume-ng-sql-source
  * https://github.com/xerial/sqlite-jdbc
  * 
* Dataset:
  * https://github.com/aliancahospitalar/backend-software-engineer-test/raw/master/twitter-movie-ratings.zip  


Where to go from here
====================
Some improvements could be implemented:

- Implement and streaming other operations on the relational tables.
- Change the insight database to redis.
- Support to a log monitoring, could also be streaming by flume.
- Try to build the pipeline with kafka.
- Create a cluster of nodes on spark.
