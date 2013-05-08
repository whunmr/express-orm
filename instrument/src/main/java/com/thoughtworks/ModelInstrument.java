package com.thoughtworks;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

public class ModelInstrument implements ClassFileTransformer {
    public static final String MODEL_CLASS = "com/thoughtworks/Model";
    public static Class transactionalAnnotationClass;
    private static final String SET_AUTOCOMMIT_TO_FALSE = "try {com.thoughtworks.DB.connection().setAutoCommit(false);} catch (java.sql.SQLException e) {e.printStackTrace();}";
    private static final String COMMIT_TRANSACTION = "try {com.thoughtworks.DB.connection().commit();} catch (java.sql.SQLException e) {e.printStackTrace();}";
    private static final String CATCH_EXCEPTION_AND_ROLLBACK = "} catch (java.lang.Exception e) {try {com.thoughtworks.DB.connection().rollback();} catch (java.sql.SQLException e1) {} throw new RuntimeException(e);}";

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ModelInstrument());
        try {
            transactionalAnnotationClass = Class.forName("com.thoughtworks.Transactional");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class clazz, java.security.ProtectionDomain domain, byte[] bytes) {
        ClassPool pool = ClassPool.getDefault();

        if (className.equals(MODEL_CLASS)) {
            return bytes;
        }

        try {
            CtClass modelClass = pool.get(MODEL_CLASS.replaceAll("/", "."));
            CtClass target = pool.get(className.replaceAll("/", "."));
            if (target.subclassOf(modelClass)) {
                addDelegateMethodInSubclassOfModel(modelClass, target);
                return target.toBytecode();
            }
        } catch (NotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (className.equals("com/thoughtworks/fixture/DummyService")) { //input the user's package
            try {
                CtClass serviceClass = pool.get(className.replaceAll("/", "."));
                CtMethod[] modelMethods = serviceClass.getDeclaredMethods();
                boolean transformed = false;
                for (CtMethod method : modelMethods) {
                    if (method.hasAnnotation(transactionalAnnotationClass)) {
                        transformTransactionalMethod(serviceClass, method);
                        transformed = true;
                    }
                }

                if (transformed) {
                    return serviceClass.toBytecode();
                }
            } catch (NotFoundException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    private void addDelegateMethodInSubclassOfModel(CtClass modelClass, CtClass target) throws CannotCompileException {
        CtMethod[] modelMethods = modelClass.getDeclaredMethods();
        List<CtMethod> targetMethods = Arrays.asList(target.getDeclaredMethods());

        for (CtMethod method : modelMethods) {
            if (Modifier.PRIVATE != method.getModifiers()) {
                if (!targetMethods.contains(method)) {
                    CtMethod newMethod = CtNewMethod.delegator(method, target);
                    target.addMethod(newMethod);
                }
            }
        }
    }

    private void transformTransactionalMethod(CtClass clazz, CtMethod method) throws NotFoundException, CannotCompileException, IOException {
        String methodName = method.getName();
        CtMethod oldMethod = clazz.getDeclaredMethod(methodName);

        String newName = methodName + "$impl";
        oldMethod.setName(newName);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, methodName, clazz, null);
        String type = oldMethod.getReturnType().getName();

        newMethod.setBody(constructWrapperMethodBody(newName, type).toString());
        clazz.addMethod(newMethod);
    }

    private StringBuffer constructWrapperMethodBody(String newName, String type) {
        StringBuffer body = new StringBuffer();

        body.append("{\ntry {" + SET_AUTOCOMMIT_TO_FALSE + "\n");
        if (!"void".equals(type)) {
            body.append(type + " result = ");
        }
        body.append(newName + "($$); " + COMMIT_TRANSACTION + "\n");
        if (!"void".equals(type)) {
            body.append("return result;\n");
        }
        body.append(CATCH_EXCEPTION_AND_ROLLBACK + "}");
        System.out.println(body);
        return body;
    }

}








