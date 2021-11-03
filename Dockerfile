FROM adoptopenjdk/openjdk11:latest

EXPOSE 8080

WORKDIR /opt/app
VOLUME /opt/app/db

COPY target/portfolio-manager-0.0.1-SNAPSHOT.jar ./

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "portfolio-manager-0.0.1-SNAPSHOT.jar"]
