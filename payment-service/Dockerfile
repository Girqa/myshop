FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app/
COPY target/payment.jar /opt/app/payment.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "payment.jar"]