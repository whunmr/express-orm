package com.thoughtworks;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

public class ModelInstrument implements ClassFileTransformer {
    public static final String MODEL_CLASS = "com/thoughtworks/Model";

    public static void premain(String agentArgument, Instrumentation instrumentation)  {
        instrumentation.addTransformer(new ModelInstrument());
    }

    public byte[] transform(ClassLoader loader, String className,Class clazz, java.security.ProtectionDomain domain,byte[] bytes) {
        ClassPool pool = ClassPool.getDefault();

        if (className.equals(MODEL_CLASS)) {
            return bytes;
        }

        try {
            CtClass modelClass = pool.get(MODEL_CLASS.replaceAll("/", "."));
            CtClass target = pool.get(className.replaceAll("/", "."));

            if (target.subclassOf(modelClass)) {
                CtMethod [] modelMethods = modelClass.getDeclaredMethods();
                List<CtMethod> targetMethods = Arrays.asList(target.getDeclaredMethods());

                for (CtMethod method : modelMethods) {
                    if (Modifier.PRIVATE != method.getModifiers()) {
                        if (!targetMethods.contains(method)) {
                            CtMethod newMethod = CtNewMethod.delegator(method, target);
                            target.addMethod(newMethod);
                        }
                    }
                }

                return target.toBytecode();
            }
        } catch (NotFoundException e){}
        catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }
}








