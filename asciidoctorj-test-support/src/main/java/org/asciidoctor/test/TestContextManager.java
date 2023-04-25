package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class TestContextManager {

    private final List<Field> sharedFields;
    private final List<Field> testFields;
    private final Asciidoctor sharedInstance;

    public TestContextManager(List<Field> sharedFields, List<Field> testFields) {
        this.sharedFields = sharedFields;
        this.testFields = testFields;
        this.sharedInstance = Asciidoctor.Factory.create();
    }

    private void injectInstance(Asciidoctor instance, Field field, Object testInstance) {
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                field.setAccessible(true);
                field.set(testInstance, instance);
                field.setAccessible(false);
            } else {
                field.set(instance, instance);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void initTestFields(Object testInstance) {
        this.testFields.forEach(field -> injectInstance(Asciidoctor.Factory.create(), field, testInstance));
    }

    public void initSharedFields(Object testInstance) {
        this.sharedFields.forEach(field -> injectInstance(sharedInstance, field, testInstance));
    }
}
