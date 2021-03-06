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

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.bytecode.Descriptor;

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

        Log.get().log("%s configured to transform the following classes: %s", getClass().getSimpleName(), this.classesToTransform);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        
        try {
            if (classesToTransform.contains(className)) {
                Log.get().log("%s asked to transform %s", getClass().getSimpleName(), className);
                ClassPool classPool = new ClassPool(true);
                // in OSGi environments access is automatically permitted to all classes, even for a Java agent
                // therefore we need to adjust the default class path
                // 1. append all classes accessible to the specified class loader
                classPool.appendClassPath(new LoaderClassPath(loader));
                // 2. insert the current definition of the class
                classPool.insertClassPath(new ByteArrayClassPath(Descriptor.toJavaName(className), classfileBuffer));
                CtClass cc = classPool.get(Descriptor.toJavaName(className));
                classfileBuffer = doTransformClass(cc);
                Log.get().log("Transformation of %s complete", className);
                this.agentInfo.registerTransformedClass(className);
            } else {
                Log.get().trace("%s did not transform %s as it was not part of the classes it handles", getClass().getSimpleName(), className);
            }
            return classfileBuffer;
        } catch (Exception e) {
            Log.get().fatal("Transformation failed", e);
            return null; // NOSONAR: null return is OK in case no transform is performed
        }
    }

    /**
     * Transform a class that is guaranteed to exist and in scope of this agent instance
     * 
     * @param cc the class
     * @return the new class definition
     * @throws Exception in case of any problems while transforming
     */
    protected abstract byte[] doTransformClass(CtClass cc) throws Exception; // NOSONAR - throwing Exception is OK, we don't want custom exceptions

}