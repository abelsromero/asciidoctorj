package org.asciidoctor.test.extension;

import org.asciidoctor.test.ClasspathResource;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.test.extension.ReflectionUtils.findFields;
import static org.asciidoctor.test.extension.ReflectionUtils.injectValue;

/**
 * Handles the initialization of {@link File} fields annotated with {@link org.asciidoctor.test.ClassResource}.
 * <p>
 * To prevent classpath traversing fields are parsed and values are initialized
 * only once and cached in {@link ClasspathExtension#postProcessTestInstance(Object, ExtensionContext)}.
 * Then assigned on every test {@link ClasspathExtension#beforeEach(ExtensionContext)}.
 * <p>
 * JUnit5's {@link org.junit.jupiter.api.extension.ExtensionContext.Store} is
 * used to hande initialization in a safe way.
 */
public class ClasspathExtension implements TestInstancePostProcessor, BeforeEachCallback, ParameterResolver {

    private static final Namespace TEST_CONTEXT_NAMESPACE = Namespace.create(ClasspathExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Class<?> contextKey = context.getRequiredTestClass();
        getStore(context)
                .getOrComputeIfAbsent(contextKey,
                        keyType -> {
                            final List<Field> resourcesFields = findFields(testInstance, File.class, ClasspathResource.class);
                            final Map<Field, File> values = matchValues(testInstance, resourcesFields);
                            return new CachedResources(values);
                        },
                        CachedResources.class);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        context.getTestInstance()
                .ifPresent(testInstance -> {
                    getStore(context)
                            .get(context.getRequiredTestClass(), CachedResources.class)
                            .initializeFields(testInstance);
                });
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(TEST_CONTEXT_NAMESPACE);
    }

    private Map<Field, File> matchValues(Object testInstance, List<Field> resourcesFields) {
        final ClasspathHelper classpathHelper = new ClasspathHelper(testInstance.getClass());
        final Map<Field, File> assignedValues = new HashMap<>();
        for (Field field : resourcesFields) {
            final String path = getAnnotationValue(field);
            final File resource = classpathHelper.getResource(path);
            assignedValues.put(field, resource);
        }
        return assignedValues;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) {
        boolean annotated = parameterContext.isAnnotated(ClasspathResource.class);
        if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
            throw new ParameterResolutionException("@ClasspathResource is not supported on constructor parameters.");
        }
        return annotated;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        Class<?> parameterType = parameter.getType();
        assertSupportedType(parameterType);

        final String path = getAnnotationValue(parameter);
        return new ClasspathHelper(context.getRequiredTestClass()).getResource(path);
    }

    private static String getAnnotationValue(AnnotatedElement parameter) {
        return parameter.getAnnotation(ClasspathResource.class).value();
    }

    private void assertSupportedType(Class<?> type) {
        if (type != File.class) {
            throw new ExtensionConfigurationException("Only File is supported");
        }
    }

    class CachedResources {

        private final Map<Field, File> cache;

        CachedResources(Map<Field, File> cache) {
            this.cache = cache;
        }

        public void initializeFields(Object testInstance) {
            cache.forEach((k, v) -> injectValue(testInstance, k, v));
        }

        public void initializeParameters(Parameter[] parameters, Object testInstance) {
            final ClasspathHelper classpathHelper = new ClasspathHelper(testInstance.getClass());
            for (Parameter param : parameters) {
                final String path = getAnnotationValue(param);
                final File resource = classpathHelper.getResource(path);
//                injectInstance(testInstance, param, resource);
                System.out.println(parameters);
            }
        }
    }
}
