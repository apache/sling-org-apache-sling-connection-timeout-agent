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

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Sets timeouts for HTTP calls done using <code>java.net.http</code>/<code>java.net.http.HttpClient</code>.
 */
class JdkHttpClientBuilderTimeoutTransformer extends MBeanAwareTimeoutTransformer {

    static final Set<String> CLASSES_TO_TRANSFORM = new HashSet<>();

    static {
        CLASSES_TO_TRANSFORM.add(Descriptor.toJvmName("jdk.internal.net.http.HttpClientBuilderImpl"));
    }

    private final long connectTimeoutMillis;

    public JdkHttpClientBuilderTimeoutTransformer(long connectTimeout, AgentInfo agentInfo) {
        
        super(agentInfo, CLASSES_TO_TRANSFORM);
        
        this.connectTimeoutMillis = connectTimeout;
    }

    protected byte[] doTransformClass(CtClass cc) throws Exception {

        CtMethod buildMethod = cc.getDeclaredMethod("build");
        buildMethod.insertBefore("if ( this.connectTimeout == null ) { connectTimeout(java.time.Duration.ofMillis(" + connectTimeoutMillis + "L)); }");
        byte[] classfileBuffer = buildMethod.getDeclaringClass().toBytecode();
        buildMethod.getDeclaringClass().detach();
        return classfileBuffer;
    }

}
