# Integration Testing
This module contains the cucumber files for BDD integration testing.

The integration tests call the HTTP rest endpoint of the running texastoc server.

Run the server locally

`mvn -Dspring-boot.run.profiles=integration-testing -pl application spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787"`

You can connect a JVM debugger on port 8787.

You can run a scenario (in IntelliJ) by clicking the green arrow by the scenario name
in a *.feature file. Or run the entire feature by clicking the green arrow next
to the feature name.

You can run the JUnit wrapper for the cucumber tests in the Cucumber*Tests class.
