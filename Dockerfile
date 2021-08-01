FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
# mvn -P [h2|mysql] -pl application clean package
# docker build -t texastoc-v4-h2-image .
ADD application/target/texastoc-v4-application-1.0.0.jar texastoc-v4-application-1.0.0.jar
ENTRYPOINT ["java","-jar","texastoc-v4-application-1.0.0.jar"]
