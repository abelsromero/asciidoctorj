package org.asciidoctor.test;

import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ClasspathExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class ClassPathResourceTest {

    @ClasspathResource("test.txt")
    private File file;

    @Order(1)
    @Test
    void should_load_resource_from_classpath_as_field() {
        assertThat(file).isNotEmpty();
    }

    @Order(2)
    @Test
    void should_load_resource_classpath_as_parameter(@ClasspathResource("test.txt") File param) {
        assertThat(param).isNotEmpty();
    }
}
