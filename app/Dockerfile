# Pull base image
FROM  openjdk:8u131-jdk
 
ENV SCALA_VERSION 2.10.6
ENV SBT_VERSION 0.13.16
 
# Scala expects this file
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

 
# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc
 
# Install sbt
RUN apt-get -y update
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \ 
  apt-get install -y sbt wget curl git maven && \
  sbt sbtVersion  


#prepare database
RUN apt-get install -y unzip
RUN apt-get install -y sqlite3 libsqlite3-dev
RUN wget https://raw.githubusercontent.com/aliancahospitalar/backend-software-engineer-test/master/twitter-movie-ratings.zip -O /tmp/db2.zip
RUN unzip /tmp/*.zip -d /db/
RUN wget http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.19.3/sqlite-jdbc-3.19.3.jar -O /db/sqlite-jdbc-driver.jar
RUN sqlite3 -noheader -separator "|" /db/twitter-movie-ratings.db "select m.id, m.title, m.year, GROUP_CONCAT(distinct(g.title)) as genres, mv.id as rating_id, r.id as rating_value, r.feeling, mv.timestamp as ratingDate  from movies m left join movie_genre mg on mg.movie_id = m.id left join genres g on g.id = mg.genre_id inner join movie_ratings mv on mv.movie_id = m.id inner join ratings r on r.id = mv.rating_id group by m.id, m.title,m.year,rating_id,rating_value, r.feeling, ratingDate " > /db/ratings.in
RUN sqlite3 /db/twitter-movie-ratings.db "CREATE TABLE EVENTS (id INTEGER PRIMARY KEY AUTOINCREMENT,target VARCHAR(30),operation VARCHAR(1), KEY_VALUE INTEGER,fields VARCHAR(500));" 
RUN sqlite3 /db/twitter-movie-ratings.db "CREATE TRIGGER INSERT_MOVIES AFTER INSERT ON movies BEGIN INSERT INTO EVENTS(TARGET,OPERATION,KEY_VALUE,FIELDS) VALUES ('MOVIES','I',new.ID, new.title || ',' || new.year ); END;" 
RUN sqlite3 /db/twitter-movie-ratings.db "CREATE TRIGGER INSERT_MOVIES_GENRES AFTER INSERT ON movie_genre BEGIN INSERT INTO EVENTS(TARGET,OPERATION,KEY_VALUE,FIELDS) VALUES ('MOVIE_GENRE','I', new.movie_id, (select title from genres where id = new.genre_id)); END;" 
RUN sqlite3 /db/twitter-movie-ratings.db "CREATE TRIGGER INSERT_MOVIE_RATINGS AFTER INSERT ON movie_ratings BEGIN  INSERT INTO EVENTS(TARGET,OPERATION,KEY_VALUE,FIELDS) VALUES ('MOVIE_RATINGS','I', new.ID, new.movie_id || ',' || new.rating_id || ',' || new.timestamp); END;" 

RUN mkdir -p /tmp/build
ADD compile-all.sh /tmp/build

ADD ./ratings-spark/ /app/ratings-spark/
ADD ./ratings-spring/ /app/ratings-spring/

RUN /tmp/build/compile-all.sh

