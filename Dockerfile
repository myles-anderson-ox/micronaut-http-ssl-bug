FROM openjdk:14-alpine
COPY build/libs/http-ssl-bug-*-all.jar http-ssl-bug.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "http-ssl-bug.jar"]