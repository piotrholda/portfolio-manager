FROM adoptopenjdk/openjdk11:latest

EXPOSE 8080
EXPOSE 8082
EXPOSE 9082

VOLUME /usr/lib/h2

ENV H2_VERSION "1.4.200"
ADD "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" /var/lib/h2/h2.jar
COPY docker/h2.sh /var/lib/h2/
RUN chmod u+x /var/lib/h2/h2.sh
ENV JAVA_OPTIONS ""
ENV H2_OPTIONS ""

RUN mkdir /opt/app
COPY target/portfolio-manager-0.0.1-SNAPSHOT.jar /opt/app

CMD /var/lib/h2/h2.sh
