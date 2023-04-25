package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;
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
        assertThat(secondSharedAsciidoctor).isNotNull();
        assertThat(firstSharedAsciidoctor).isSameAs(secondSharedAsciidoctor);

        assertThat(firstMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(secondMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(firstMethodAsciidoctor).isNotSameAs(secondMethodAsciidoctor);
    }


}
