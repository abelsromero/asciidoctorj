package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AsciidoctorExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class MultipleResourceTest {

    @ClassResource
    private Asciidoctor firstSharedAsciidoctor;
    @ClassResource
    private Asciidoctor secondSharedAsciidoctor;

    @TestMethodResource
    private Asciidoctor firstMethodAsciidoctor;
    @TestMethodResource
    private Asciidoctor secondMethodAsciidoctor;

    private static Asciidoctor copy;

    @Order(1)
    @Test
    void should_inject_asciidoctor() {
        assertThat(firstSharedAsciidoctor).isNotNull();
        assertThat(secondSharedAsciidoctor)
                .isNotNull()
                .isSameAs(firstSharedAsciidoctor);

        assertThat(firstMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(secondMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(firstMethodAsciidoctor).isNotSameAs(secondMethodAsciidoctor);

        copy = firstSharedAsciidoctor;
    }

    @Order(2)
    @Test
    void should_only_initialize_new_shared_asciidoctor_instances() {
        assertThat(firstSharedAsciidoctor)
                .isNotNull()
                .isSameAs(secondSharedAsciidoctor)
                .isSameAs(copy);
        assertThat(secondSharedAsciidoctor)
                .isNotNull()
                .isSameAs(copy);

        assertThat(firstMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(copy);
        assertThat(secondMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(copy);
        assertThat(firstMethodAsciidoctor).isNotSameAs(secondMethodAsciidoctor);
    }
}
