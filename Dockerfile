# export DB=[h2|mysql]
# mvn -P $DB -pl application clean package
# docker build -t texastoc-v4-$DB-image .
FROM adoptopenjdk/openjdk11:latest
EXPOSE 8080
COPY application/target/texastoc-v4-application-1.0.0.jar /application/
ADD third-party/newrelic-java /application/
ENTRYPOINT ["java"]
CMD [ \
    "-javaagent:/application/newrelic/newrelic.jar", \
    "-jar", \
    "/application/texastoc-v4-application-1.0.0.jar" \
]
