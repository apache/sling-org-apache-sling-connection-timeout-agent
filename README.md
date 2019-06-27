# Apache Sling HTTP timeout enforcer

This module is part of the [Apache Sling](https://sling.apache.org) project.

This module provides a java agent that uses the [instrumentation API](https://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html) to add connect and read timeouts to `connect` made via HTTP or HTTPs. It only applies these timeouts if none were set explicitly.

End-user documentation can be found on the Apache Sling Website in the [Connection Timeout Agent](https://sling.apache.org/documentation/bundles/connection-timeout-agent.html) section.

## Validation

In addition to running the integration tests, you can also build the project with `mvn clean package` and then run a simple connection test with 

    java -javaagent:target/org.apache.sling.connection-timeout-agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar=<agent-connect-timeout>,<agent-read-timeout> -cp target/test-classes:target/it-dependencies/* org.apache.sling.cta.impl.HttpClientLauncher <url> <client-type> [<client-connect-timeout> <client-read-timeout>]
    
 The parameters are as follows:
 
 - `<agent-connect-timeout>` - connection timeout in milliseconds to apply via the agent
 - `<agent-read-timeout>`- read timeout in milliseconds to apply via the agent
 - `<url>` - the URL to access
 - `<client-type>` - the client type, either `JavaNet` for java.net.URL-based connections ,`HC3` for Apache Commons HttpClient 3.x, `HC4` for Apache Commons HttpClient 4.x or `OkHttp` for OK HTTP.
 - `<client-connect-timeout>` (optional) - the connection timeout in milliseconds to apply via client APIs
 - `<client-read-timeout>` (optional) - the read timeout in milliseconds to apply via client APIs
 
The read and connect timeouts may be specified for both the agent and client APIs. The reason is that the agent should not change the timeout defaults if they are already set. Therefore, setting the agent timeouts to a very high value and the client API timeouts to a very low value ( e.g. 1 millisecond ) should still result in a timeout. 
 
 
 For a test that always fails, set one of the timeouts to 1. Both executions listed below will typically fail:
 
 ```
java -javaagent:target/org.apache.sling.connection-timeout-agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar=1,1000 -cp target/test-classes:target/it-dependencies/* org.apache.sling.cta.impl.HttpClientLauncher https://sling.apache.org JavaNet
java -javaagent:target/org.apache.sling.connection-timeout-agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar=1000,1 -cp target/test-classes:target/it-dependencies/* org.apache.sling.cta.impl.HttpClientLauncher https://sling.apache.org JavaNet
 ```
 
In contrast, the execution below should succeed:

```
java -javaagent:target/org.apache.sling.connection-timeout-agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar=1000,1000 -cp target/test-classes:target/it-dependencies/* org.apache.sling.cta.impl.HttpClientLauncher https://sling.apache.org JavaNet
```