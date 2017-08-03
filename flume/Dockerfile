FROM probablyfine/flume:latest
#RUN mkdir /opt/hadoop
#RUN wget -qO- http://archive.apache.org/dist/hadoop/common/hadoop-2.4.0/hadoop-2.4.0.tar.gz \
#  | tar zxvf - -C /opt/hadoop --strip 1

RUN mkdir -p /opt/flume/plugins.d/sql-source/lib /opt/flume/plugins.d/sql-source/libext
#ENV HADOOP_HOME="/opt/hadoop"
RUN wget http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.19.3/sqlite-jdbc-3.19.3.jar -O /opt/flume/plugins.d/sql-source/libext/sqlite-jdbc-3.19.3.jar
RUN wget http://central.maven.org/maven2/com/enigmabridge/hibernate4-sqlite-dialect/0.1.2/hibernate4-sqlite-dialect-0.1.2.jar -O /opt/flume/plugins.d/sql-source/libext/hibernate4-sqlite-dialect-0.1.2.jar

ADD lib/flume-ng-sql-source-1.4.4.jar /opt/flume/plugins.d/sql-source/lib/flume-ng-sql-source-1.4.4.jar
ADD flume.conf /opt/flume/flume.conf
WORKDIR /opt/flume 
ENTRYPOINT ["bin/flume-ng", "agent"]