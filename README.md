# texastoc-v4

Refactor version 3 to version 4.

Version 4 will
* refactor the cucumber integration tests to JUnit
* allow multiple season for a year for testing only
* require the season Id for the url paths for game endpoints

# Profiles, Building and Running

The spring boot application can be run

* in an IDE (this README only covers IntelliJ)
* maven command line
* as a war and run using the webapp-runner.jar
* as a war deployed to an installed tomcat

There are multiple profile settings for building and running the Spring Boot Server: a the maven profile setting and spring profile(s) setting.

### Maven Profile

Maven profiles are defined in the <profiles> section in the top level pom.xml. The maven profile is used to bring in libraries (i.e. dependencies). The following profiles are defined:

* h2-embedded-tomcat
* h2-embedded-tomcat-spring-integration
* mysql-embedded-tomcat-spring-integration
* mysql-provided-tomcat-spring-integration

A maven profile is specified on the command line by using the "-P" flag (e.g. -P dev). A maven profile can be specified in IntelliJ by selecting a profile from the list of Profiles in the maven tool window.

The maven h2-embedded-tomcat profile

1. brings in H2
2. brings in an embedded tomcat (i.e. tomcat runs in the same JVM as the application)

The maven h2-embedded-tomcat-spring-integration profile

1. brings in H2
2. brings in an embedded tomcat (i.e. tomcat runs in the same JVM as the application)
3. brings in spring integration (used for messaging)

The maven mysql-embedded-tomcat-spring-integration profile

1. brings in MySQL
2. brings in an embedded tomcat (i.e. tomcat runs in the same JVM as the application)
3. brings in spring integration (used for messaging)

### Spring Profiles

Spring profiles, if set, can be used to conditionally include spring configuration, beans, ... at **runtime**.

When running the mvn command line tool the active spring profile can be set using the *-Dspring-boot.run.profiles=foo,bar* command line argument.

To set the spring runtime profile in IntelliJ use the "VM options" with the value *-Dspring.profiles.active=foo*.

To set the spring runtime profile in when running the war file set *-Dspring.profiles.active=foo,bar*.

when the Spring application starts up if the default *application.yml* file is found the properites in that file are configured as runtime properties. For each Spring profile (e.g. foo) if an corresponding properties file is found (e.g. application-foo.yml) then the properties in that file will also be configured as runtime properties.

You will see the following property files

* application-h2.yml
* application-message-events.yml
* application-message-rest.yml
* application-mysql.yml
* application-populate.yml

Note that to run the server the profile for the database and the profile for the messaging MUST be defined.

The application-h2.yml is used to connect to the H2 database and also to initialize the database schema and seed the data. The file to create the database tables (schema) can be found in the *create_toc_schema.sql* file. The file to seed the tables can be found in the *seed_toc.sql* file.

The application-message-events.yml properties enables the Spring configuration to use messaging.

The application-message-rest.yml properties causes the application to make REST calls in lieu of messaging. Why you ask? Because Heroku has a bug and will not start the application when this application is configure to use spring integration for messaging. I hope to remove this work around when Heroku fixes the problem.

The application-mysql.yml properties are used connect to an external MySQL database.

The application-populate.yml properties cause a population job to run to load up the application with multiple games for a season.

### Running the server

#### IntelliJ

To run in IntelliJ select one maven profile in the maven tool window. Right click on the Application class and select Run from the popup window. Remember to set which Spring profiles are to be active in the "VM options" of the run configuration.

#### maven command line

To run with `mvn` again defined the maven profile on the command line with "-P" and which Spring profiles are to be active with "-Dspring-boot.run.profiles" all followed by "-pl application spring-boot:run".

Example of building/running with H2, embedded tomcat, populate data and use spring integration for messaging:

* *mvn -P h2-embedded-tomcat-spring-integration -Dspring-boot.run.profiles=h2,populate,message-events -pl application spring-boot:run*

Note that the `-pl application` part of the command instructs maven to run the application module which is where the server code is. There is another module called integration which, when run, runs the integration tests.

#### webapp-runner

The war can be run in webapp-runner. This is how the server is run when deployed to Heroku.

* Build a war file by typing `mvn -P h2-embedded-tomcat -pl application clean package`. In this example the war is built the same way it is built when running on Heroku (i.e. H2 and embedded tomcat).
* Run the server by typing `java -Dspring.profiles.active=h2,populate,message-rest -jar application/target/dependency/webapp-runner.jar application/target/texastoc-v4-application-1.0.war`. In this example the war is run the same way it is run on Heroku (i.e. H2, populate the data and user REST for messaging).

#### war deployed to Heroku

* Build and deploy the war file by typing `mvn -P h2-embedded-tomcat -pl application clean heroku:deploy-war`
* Tail the Heroku logs: `heroku logs --app texastoc-server --tail`

Note that the active spring profiles have to be configured in the Heroku config JAVA_OPTS variable as *-Dspring.profiles.active=h2,populate,message-rest*

#### war deployed to an external tomcat

Remember to have a running MySQL database for the server to use. Obviously must also have tomcat installed.

* Build the war A war file by typing `mvn -P mysql-provided-tomcat-spring-integration -pl application clean package`

For production deploy the war to a tomcat installation and remember to set the active spring profiles. The JAVA_OPTS variable can be set in tomcat's setenv.sh file as follows:

* JAVA_OPTS="-Dspring.profiles.active=mysql,message-events"

#### Debugging

To run the dev server in debug mode from the maven command line: `mvn -P h2-embedded-tomcat-spring-integration -Dspring-boot.run.profiles=h2,populate,message-events -pl application spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"`

# Connect to the H2 server

When running the dev server the H2 database can be access as follows:

* open `http://localhost:8080/h2-console` url.
* set the JDBC URL to `jdbc:h2:mem:testdb`
* User Name `sa`
* Leave the password empty
* Click Connect

# Testing

There are unit tests and integration tests.

### Unit tests

You can run the unit tests in IntelliJ or from the command line.

To run in IntelliJ right click on the java folder and choose _Run 'All Tests'_

* application -> src -> test -> java

To run all the tests from the command line type

* mvn test

### Integration tests

To run the one or more integration tests the application first has to be started. For example (with debugging)

* *mvn -P h2-embedded-tomcat-spring-integration -Dspring-boot.run.profiles=h2,message-events -pl application spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"*

To run in IntelliJ right click on the java folder and choose _Run 'All Tests'_

* integration -> src -> test -> java

# WebSocket

On branch 54-clock-web-socket added a websocket to the server. In the future this websocket will be used to communicate a running clock to the client.

The client is going to first use polling so the websocket requirement has be put on hold.

# SSL certificate

Using LetsEncrypt for the SSL certificate.

To generate/renew

```
certbot certonly \
  --manual \
  --preferred-challenges=dns \
  --email <my email> \
  --agree-tos \
  --config-dir ./config \
  --logs-dir ./logs \
  --work-dir ./work \
  --cert-name texastoc.com \
  -d texastoc.com \
  -d www.texastoc.com
```

# Branches

The branch labels are prefixed in the order they were developer (e.g. 01-, 02, ...).

Choose the branch from the github list of branches to see the readme for that branch.

To see the code for a branch compare the branch to the previous branch.

## Current Branch: 02-remove-current-season-and-game
Removed the endpoints to get the current season and get the current game.
Now have to get them by Id.

Refactored all the remaining cucumber tests to JUnit.
