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
import java.net.SocketTimeoutException;

import org.apache.sling.cta.impl.HttpClientLauncher.ClientType;

/**
 * Data class for defining specific error messages related to individual {@link ClientType client types}.
 */
class ErrorDescriptor {
    Class<? extends IOException> connectTimeoutClass;
    String connectTimeoutMessageRegex;
    Class<? extends IOException> readTimeoutClass;
    String readTimeoutRegex;

    public ErrorDescriptor(
            Class<? extends IOException> connectTimeoutClass,
            String connectTimeoutMessageRegex,
            String readTimeoutRegex) {
        this(connectTimeoutClass, connectTimeoutMessageRegex, SocketTimeoutException.class, readTimeoutRegex);
    }

    public ErrorDescriptor(
            Class<? extends IOException> connectTimeoutClass,
            String connectTimeoutMessageRegex,
            Class<? extends IOException> readTimeoutClass,
            String readTimeoutRegex) {
        this.connectTimeoutClass = connectTimeoutClass;
        this.connectTimeoutMessageRegex = connectTimeoutMessageRegex;
        this.readTimeoutClass = readTimeoutClass;
        this.readTimeoutRegex = readTimeoutRegex;
    }
}
