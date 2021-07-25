FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
# For building with embedded in memory H2
# mvn -P h2-embedded-tomcat-spring-integration -pl application clean packageADD application/target/texastoc-v4-application-1.0.war texastoc-v4-application-1.0.war
ADD application/target/texastoc-v4-application-1.0.war texastoc-v4-application-1.0.war
ENTRYPOINT ["java","-Dspring.profiles.active=h2,populate","-jar","texastoc-v4-application-1.0.war"]
