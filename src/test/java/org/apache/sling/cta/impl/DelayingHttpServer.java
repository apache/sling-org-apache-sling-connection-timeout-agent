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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayingHttpServer {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Duration handleDelay;
    
    public DelayingHttpServer(Duration handleDelay) {
        this.handleDelay = handleDelay;
    }
    
    private Server server;
    
    public void start() throws Exception {
        server = new Server(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        server.setHandler(new AbstractHandler() {
            
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                logger.info("Waiting for {} before handling", handleDelay);
                try {
                    Thread.sleep(handleDelay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                if ( baseRequest.getHeader("User-Agent") != null )
                    response.addHeader("Original-User-Agent", baseRequest.getHeader("User-Agent"));
                baseRequest.setHandled(true);
                logger.info("Handled");
            }
        });
        
        server.start();
    }
    
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
    
    public int getLocalPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public void setHandleDelay(Duration handleDelay) {
        this.handleDelay = handleDelay;
    }
}