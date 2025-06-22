FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app/
COPY target/myshop.jar /opt/app/myshop.jar

EXPOSE 8080 8000
ENTRYPOINT ["java", "-jar", "myshop.jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"]