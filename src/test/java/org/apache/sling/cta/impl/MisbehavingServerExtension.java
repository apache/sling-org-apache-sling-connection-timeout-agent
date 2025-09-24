/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.cta.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Jetty-based local server that can be configured to timeout
 *
 * <p>After extending a JUnit Jupiter test with this extension, any parameter of type {@link MisbehavingServerControl}
 * will be resolved.</p>
 *
 */
class MisbehavingServerExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver, MisbehavingServerControl {

    private static final Duration DEFAULT_HANDLE_DELAY = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DelayingHttpServer server;

    private ServerSocket ss;
    private List<Socket> sockets = new ArrayList<>();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == MisbehavingServerControl.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == MisbehavingServerControl.class) return this;

        throw new ParameterResolutionException("Unable to get a " + MisbehavingServerControl.class.getSimpleName()
                + " instance for " + parameterContext);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

        server = new DelayingHttpServer(DEFAULT_HANDLE_DELAY);
        server.start();

        // an undocumented feature of ServerSocket is that the backlog size is quietly adjusted
        // to be at least 50
        int backlog = 50;

        // create a server socket that will not accept connections. We do this by controlling the
        // backlog size and making sure that it is full before running the test
        ss = new ServerSocket(0, backlog, InetAddress.getLoopbackAddress());

        CountDownLatch waitForConnection = new CountDownLatch(1);

        new Thread(() -> {
                    int activeConnection = 0;
                    try {
                        // completely tie up the server: 1 active connection + backlog full
                        for (int i = 0; i < backlog + 1; i++) {
                            activeConnection = i;
                            sockets.add(new Socket("127.0.0.1", ss.getLocalPort()));
                        }
                        logger.info("Keeping connections to port {} unavailable", ss.getLocalPort());
                        waitForConnection.countDown();
                        // Keep the connection open to fill the backlog
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        logger.info("Failed connecting to server, active connection was {}", activeConnection, e);
                        waitForConnection.countDown();
                    }
                })
                .start();

        waitForConnection.await();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.info("Failed shutting down server", e);
            }
        }

        if (ss != null) {
            try {
                ss.close();
            } catch (IOException e) {
                logger.info("Failed closing server socket", e);
            }
        }

        for (Socket s : sockets) {
            try {
                s.close();
            } catch (IOException e) {
                logger.info("Failed closing socket", e);
            }
        }
        sockets.clear();
    }

    @Override
    public void setHandleDelay(Duration handleDelay) {
        server.setHandleDelay(handleDelay);
    }

    @Override
    public int getLocalPort() {
        return server.getLocalPort();
    }

    @Override
    public int getConnectTimeoutLocalPort() {
        return ss.getLocalPort();
    }
}
