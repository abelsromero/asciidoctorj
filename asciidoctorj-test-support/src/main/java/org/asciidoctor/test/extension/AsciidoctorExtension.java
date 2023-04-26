package org.asciidoctor.test.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.ClassResource;
import org.asciidoctor.test.TestMethodResource;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.asciidoctor.test.extension.ReflectionUtils.findFields;
import static org.asciidoctor.test.extension.ReflectionUtils.hasAnnotation;


/**
 * Injects and Asciidoctor instance in any field of type {@link Asciidoctor} annotated with
 * either {@link ClassResource} or {@link TestMethodResource}.
 * <p>
 * Note that each field will receive its own instance regardless of the initialization method.
 */
public class AsciidoctorExtension implements TestInstancePostProcessor, BeforeEachCallback {

    private static final Namespace TEST_CONTEXT_NAMESPACE = Namespace.create(AsciidoctorExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Class<?> contextKey = context.getRequiredTestClass();
        getStore(context)
                .getOrComputeIfAbsent(contextKey,
                        keyType -> {
                            final List<Field> asciidoctorFields = findFields(testInstance, Asciidoctor.class);
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
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(TEST_CONTEXT_NAMESPACE);
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
    }
}
