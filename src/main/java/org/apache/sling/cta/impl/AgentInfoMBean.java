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

/**
 * Exposes runtime information about the agent using <code>JMX</code>.
 *
 */
public interface AgentInfoMBean {

    /**
     * Returns the connect timeout
     * 
     * @return the connect timeout as configured, in milliseconds
     */
    long getConnectTimeoutMillis();

    /**
     * Returns the read timeout
     * 
     * @return the read timeout as configured, in milliseconds
     */
    long getReadTimeoutMillis();
    
    /**
     * Returns the active transformers
     * 
     * @return the active transformers
     */
    String[] getTransformers();
    
    /**
     * Returns the classes that were transformed to enforce global timeout defaults
     * 
     * @return the classes that were transformed
     */
    String[] getTransformedClasses();
}
