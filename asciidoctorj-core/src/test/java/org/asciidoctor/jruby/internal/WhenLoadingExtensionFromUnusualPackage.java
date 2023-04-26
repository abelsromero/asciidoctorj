package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.test.TestMethodResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import unusual.extension.BoldifyPostProcessor;

@ExtendWith(AsciidoctorExtension.class)
public class WhenLoadingExtensionFromUnusualPackage {

    @TestMethodResource
    private Asciidoctor asciidoctor;

    @Test
    public void shouldAllowLoadingUsingInstance() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(new unusual.extension.BoldifyPostProcessor());
    }

    @Test
    public void shouldAllowLoadingByClassName() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(BoldifyPostProcessor.class.getCanonicalName());
    }

    @Test
    public void shouldAllowLoadingByClass() {
        final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
        registry.postprocessor(BoldifyPostProcessor.class);
    }
}
