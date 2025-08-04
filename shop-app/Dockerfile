FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app/
COPY target/myshop.jar /opt/app/myshop.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "myshop.jar"]