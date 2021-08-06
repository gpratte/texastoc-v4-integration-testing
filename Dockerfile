# export DB=[h2|mysql]
# mvn -P $DB -pl application clean package
# docker build -t texastoc-v4-$DB-image .
FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
ADD application/target/texastoc-v4-application-1.0.0.jar texastoc-v4-application-1.0.0.jar
ENTRYPOINT ["java","-jar","texastoc-v4-application-1.0.0.jar"]
