FROM probablyfine/flume

# libs
RUN apt-get -y update
RUN apt-get install -y unzip
RUN apt-get install -y sqlite3 libsqlite3-dev

RUN mkdir -p /opt/flume/plugins.d/sql-source/lib /opt/flume/plugins.d/sql-source/libext
RUN wget http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.19.3/sqlite-jdbc-3.19.3.jar -O /opt/flume/plugins.d/sql-source/libext/sqlite-jdbc-3.19.3.jar
RUN wget http://central.maven.org/maven2/com/enigmabridge/hibernate4-sqlite-dialect/0.1.2/hibernate4-sqlite-dialect-0.1.2.jar -O /opt/flume/plugins.d/sql-source/libext/hibernate4-sqlite-dialect-0.1.2.jar
ADD lib/flume-ng-sql-source-1.4.4.jar /opt/flume/plugins.d/sql-source/lib/flume-ng-sql-source-1.4.4.jar

#CONF/SCRIPTS
ADD flume.conf /opt/flume/flume.conf
ADD start-flume.sh /opt/flume/bin/start-flume

	