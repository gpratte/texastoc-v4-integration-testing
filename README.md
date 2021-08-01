# texastoc-v4

Refactor version 3 to version 4.

Version 4 will

* refactor the cucumber integration tests to JUnit
* allow multiple season for a year for testing only
* require the season Id for the url paths for game endpoints
* Dockerize the applications

# Profiles, Building and Running

The spring boot application can be run

* in an IDE (this README only covers IntelliJ)
* maven command line
* as a jar from the command line
* as a Docker container

### Maven Profile

Maven profiles are defined in the <profiles> section in the top level pom.xml. The maven profile is
used to bring in libraries (i.e. dependencies). The following profiles are defined:

* h2
* mysql

A maven profile can be specified on the command line by using the "-P" flag (e.g. -P h2). A maven
profile can be specified in IntelliJ by selecting a profile from the list of Profiles in the maven
tool window.

The maven h2 profile brings in the libraries to access an in-memory H2 database.

The maven mysql profile brings in the libraries to access an external MySQL database.

### Environment Variables

Here is a snippet from the application.yml file

```
db:
  h2: true
  mysql: false
  # There are other fields but they should be set as environment variables.
  # schema: false
  # seed: false
  # populate: false
```
The properties in the application.yml file are used by the server at runtime. The
```
db:
  h2: true
  mysql: false
```
properties translate into the server loading the the connection information to connect to the H2 database.

### Running the server

#### IntelliJ

To run in IntelliJ select one maven profile in the maven tool window. Right click on the Application
class and select Run from the popup window. Other environment variables can be set on the run
configuration. For example
```
DB_SCHEMA=true;DB_SEED=true;DB_POPULATE=true
```
Running in IntelliJ is obviously the easiest way to step through code in the debugger.

#### maven command line
To run with `mvn` define the maven profile on the command line with `-P`.

Environment variables can be passed on the command line or set as variables in the terminal window.

Here's an example of using the `h2` profile and some command line paramters
```
mvn -Dspring-boot.run.arguments="--db.h2=true --db.mysql=false --db.schema=true --db.seed=true --db.populate=true"
-P h2 -pl application spring-boot:run
```
The variable can be set in the terminal (Mac) which runs the server. Set an environment variable like so
```
export DB_H2=true
export DB_MYSQL=false
export DB_SCHEMA=true
export DB_SEED=true
export DB_POPULATE=true
```
and then run the server like so
which Spring
profiles are to be active with "-Dspring-boot.run.profiles" all followed by "-pl application
spring-boot:run".
```
mvn -P h2 -pl application spring-boot:run
```
Note that an environment variable can be removed by `unset`. For example
```
unset DB_H2
```
The `mvn` command can be augmented with the following to allow a debugger to connect on port 8787
```
-Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"
```

#### jar command line
Before running a jar it has to be built. Here is the command to build a jar with the H2 libraries
(obviously change `h2` to `mysql` to build a jar with the MySQL libraries)
```
mvn -P h2 -pl application clean package
```
Run the jar like so
```
java -jar application/target/texastoc-v4-application-1.0.0.jar
```
Here is an example of passing parameters
```
java -jar application/target/texastoc-v4-application-1.0.0.jar --db.h2=true --db.mysql=false --db.schema=true --db.seed=true --db.populate=true
```
As was true for running with `mvn` the same is true when running a jar for exposing port 8787 for debugging
```
java -jar application/target/texastoc-v4-application-1.0.0.jar --db.h2=true --db.mysql=false --db.schema=true --db.seed=true --db.populate=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787
```

#### Docker
The server can be run in a standalone container when using the in-memory H2 database or alongside
a MySQL container.

###### For running with H2
First build the jar
```
mvn -P h2 -pl application clean package
```
Next build the Docker image
```
docker build -t texastoc-v4-h2-image .
```
A snippet of how the image looks
```
REPOSITORY                TAG
texastoc-v4-h2-image      latest
```
You will set the environment either in the terminal or in the docker-compose file.
Here's the enviroment variables being set in the `docker-compose-server-h2.yml` file
```
environment:
  SPRING_APPLICATION_JSON: >
    {
      "db": {
        "h2": true,
        "mysql": false,
        "schema": true,
        "seed": true,
        "populate": true
      }
    }
```
Bring up the standalone container `docker compose -f docker-compose-server-h2.yml up`

###### For running with MYSQL
First configure MySQL standalone. Bring up MySQL `docker compose -f docker-compose-mysql.yml up -d` (this will create a `./data` directory fyi).

Now bash into the server, set the root password, create another user and create a database.
* docker exec -it db bash
* mysql -u root -p
* ALTER USER 'root'@'localhost' IDENTIFIED BY 'shipit';
* CREATE USER 'tocuser'@'%' IDENTIFIED BY 'shipit';
* GRANT ALL PRIVILEGES ON * . * TO 'tocuser'@'%';
* FLUSH PRIVILEGES;
* quit;
* mysql -u tocuser -p
* create database toc;
* quit;
and then stop the container `docker compose -f docker-compose-mysql.yml down`

Build the jar
```
mvn -P mysql -pl application clean package
```
Next build the Docker image
```
docker build -t texastoc-v4-mysql-image .
```
A snippet of how the image looks
```
REPOSITORY                TAG
texastoc-v4-mysql-image   latest
```
The same goes for the enviroment variables as describe above for running the standalone H2 server.
Bring up the containers `docker compose -f docker-compose-server-mysql.yml up`

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

To run the one or more integration tests the application first has to be started. For example (with
debugging)

* mvn -Dspring-boot.run.arguments="--db.h2=true --db.mysql=false --db.schema=true --db.seed=true --db.populate=false"

To run in IntelliJ right click on the java folder and choose _Run 'All Tests'_

* integration -> src -> test -> java

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

## Current Branch: 13-more-docker
Flesh out the Dockerfile, docker-compose yaml files and vastly overhaul the README
(e.g. no longer build a war and deploy it to a standalone Tomcat).
