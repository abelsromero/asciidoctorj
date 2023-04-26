package org.asciidoctor;


import org.asciidoctor.test.ClassResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

@ExtendWith(AsciidoctorExtension.class)
public class WhenSourceHighlightingIsUsed {

    private static final String DOCUMENT = "[source,java]\n" +
            "----\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    println(\"Hello World\")\n" +
            "  }\n" +
            "}\n" +
            "----\n";

    @ClassResource
    private Asciidoctor asciidoctor;

    @Test
    public void should_render_with_rouge() {
        String html = asciidoctor.convert(DOCUMENT,
                OptionsBuilder.options()
                        .standalone(true)
                        .safe(SafeMode.UNSAFE)
                        .attributes(
                                AttributesBuilder.attributes()
                                        .sourceHighlighter("rouge")));

        Document doc = Jsoup.parse(html);

        assertThat("No elements were highlighted", doc.select("pre.rouge span.kd").size(), greaterThan(0));
        assertThat("CSS was not added", html, containsString("pre.rouge .kd"));
    }

    @Test
    public void should_render_with_coderay() {
        String html = asciidoctor.convert(DOCUMENT,
                OptionsBuilder.options()
                        .standalone(true)
                        .safe(SafeMode.UNSAFE)
                        .attributes(
                                AttributesBuilder.attributes()
                                        .sourceHighlighter("coderay")));


        Document doc = Jsoup.parse(html);

        assertThat("No elements were highlighted", doc.select("pre.CodeRay span.class").size(), greaterThan(0));
        assertThat("CSS was not added", html, containsString(".CodeRay .class"));
    }
}
