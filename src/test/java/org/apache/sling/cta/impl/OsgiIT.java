/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.cta.impl;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

/**
 * Smoke test to ensure that the agent works as expected inside an OSGi container
 * 
 * <p>It only validates one scenario, and it's not terribly important which one, just that it uses
 * a library, and not the built-in HttpClient, as the class loading requirements are more strict.</p>
 *
 */
@RunWith(PaxExam.class)
public class OsgiIT {
    
    private DelayingHttpServer server;

    @Configuration
    public Option[] config() throws IOException {
        
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher = fs.getPathMatcher("glob:org.apache.sling.connection-timeout-agent-*-jar-with-dependencies.jar");
        
        List<Path> agentCandidates = Files.list(Paths.get("target"))
            .filter(Files::isRegularFile)
            .filter(p -> matcher.matches(p.getFileName()))
            .collect(Collectors.toList());
                
        if ( agentCandidates.size() != 1 )
            throw new RuntimeException("Expected exactly one agent jar, but found " + agentCandidates);
        
        return options(
            junitBundles(),
            mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.9.26"),
            mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.4.12"),
            mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.5.10"),
            mavenBundle("org.apache.felix", "org.apache.felix.http.servlet-api", "3.0.0"),
            mavenBundle("org.apache.felix","org.apache.felix.http.jetty","5.1.26"),
            vmOption("-javaagent:" + agentCandidates.get(0) +"=10000,1,v") // large connect timeout, very small read timeout
        );
    }
    
    @Before
    public void startHttpServer() throws Exception {
        server = new DelayingHttpServer(Duration.ofSeconds(1));
        server.start();
    }
    
    @After
    public void stopHttpServer() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test(expected = SocketTimeoutException.class)
    public void callTimesOut() throws IOException {
        try ( CloseableHttpClient httpclient = HttpClients.createDefault() ) {
            // the used host does not really matter, the connect timeout of 1 ms
            // should kick in almost instantly
            HttpGet get = new HttpGet("http://127.0.0.1:" + server.getLocalPort() + "/");
            try ( CloseableHttpResponse response = httpclient.execute(get)) {
                fail("Request should have failed");
            } 
        }
    }
    
}
