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
package org.apache.sling.uca.impl;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;

/**
 * Support class for transformers that expose runtime information through JMX
 * 
 * <p>All transformer implementations should extend from this base class.</p>
 *
 */
public abstract class MBeanAwareTimeoutTransformer implements ClassFileTransformer {

    private final AgentInfo agentInfo;
    private final Set<String> classesToTransform;

    public MBeanAwareTimeoutTransformer(AgentInfo agent, Set<String> classesToTransform) {
        this.agentInfo = agent;
        this.classesToTransform = classesToTransform;
        this.agentInfo.registerTransformer(getClass());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (classesToTransform.contains(className)) {
                Log.get().log("%s asked to transform %s", getClass().getSimpleName(), className);
                classfileBuffer = doTransformClass(className);
                Log.get().log("Transformation complete.");
                
                this.agentInfo.registerTransformedClass(className);
            }
            return classfileBuffer;
        } catch (Exception e) {
            Log.get().fatal("Transformation failed", e);
            return null;
        }
    }

    protected abstract byte[] doTransformClass(String className) throws Exception;

}