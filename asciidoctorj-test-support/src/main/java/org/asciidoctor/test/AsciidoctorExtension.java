package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Injects and Asciidoctor instance in any field of type {@link Asciidoctor} annotated with
 * either {@link ClassResource} or {@link TestMethodResource}.
 * <p>
 * Note that each field will receive its own instance regardless of the initialization method.
 */
public class AsciidoctorExtension implements TestInstancePostProcessor, BeforeEachCallback {

    private static final ExtensionContext.Namespace TEST_CONTEXT_MANAGER_NAMESPACE = ExtensionContext.Namespace.create(AsciidoctorExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        ExtensionContext.Store store = getStore(context);
        TestContextManager tcm = store
                .getOrComputeIfAbsent(testClass,
                        aClass -> {
                            final List<Field> asciidoctorFields = findAsciidoctorFields(testInstance);
                            final List<Field> sharedFields = new ArrayList<>();
                            final List<Field> notSharedFields = new ArrayList<>();
                            for (Field field : asciidoctorFields) {
                                if (hasAnnotation(field, ClassResource.class)) {
                                    sharedFields.add(field);
                                } else if (hasAnnotation(field, TestMethodResource.class)) {
                                    notSharedFields.add(field);
                                }
                            }
                            return new TestContextManager(sharedFields, notSharedFields);
                        },
                        TestContextManager.class);
        System.out.println(tcm);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(TEST_CONTEXT_MANAGER_NAMESPACE);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle
        context.getTestInstance()
                .ifPresent(testInstance -> {
                    var tcm = getStore(context)
                            .get(context.getRequiredTestClass(), TestContextManager.class);
                    tcm.initSharedFields(testInstance);
                    tcm.initTestFields(testInstance);
                });
//        Object instanceFromContext = testInstance;
//        Object instanceFromTcm = testContextManager.getInstance();

    }

    private boolean hasAnnotation(Field field, Class<? extends Annotation> annotation) {
        return field.getAnnotation(annotation) != null;
    }

    private List<Field> findAsciidoctorFields(Object testInstance) {
        return Arrays.stream(testInstance.getClass().getDeclaredFields())
                .filter(field -> Asciidoctor.class.isAssignableFrom((field.getType())))
                .collect(Collectors.toList());
    }
}
