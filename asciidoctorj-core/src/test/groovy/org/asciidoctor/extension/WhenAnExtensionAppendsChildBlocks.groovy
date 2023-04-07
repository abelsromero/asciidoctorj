package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Section
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenAnExtensionAppendsChildBlocks extends Specification {

    String document = '''= Test document

== Section 1

some text

== Section 2

more text

'''

    @ArquillianResource
    private Asciidoctor asciidoctor

    def 'should be able to blocks via Block_append'() {

        given:

        final String additionalText = 'Text added by Treeprocessor'
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {

            int lastid = 0

            @Override
            Document process(Document document) {
                document.blocks.findAll { block -> block instanceof Section }.each {
                    block ->
                        Block newBlock = createBlock(block, 'paragraph', additionalText, [:])
                        newBlock.id = "NewBlock_${lastid++}"
                        block.append(newBlock)
                }
                document
            }
        })

        when:
        String result = asciidoctor.convert(this.document, OptionsBuilder.options().standalone(false))

        then:
        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(result)
        !htmlDocument.select('#NewBlock_0').empty
        !htmlDocument.select('#NewBlock_1').empty
        htmlDocument.select('#NewBlock_2').empty

    }

    def "should be able to append and parse raw asciidoc content via Block_parse"() {

        given:
        final String newContentClass = 'newcontent'
        final List<String> contentToAdd = """
[.$newContentClass]
This is content added by an extension.

[.$newContentClass]
And this as well
""".readLines()

        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            Document process(Document document) {
                document.blocks.findAll { block -> block instanceof Section }.each {
                    section ->
                        parseContent(section, contentToAdd)
                }
                document
            }
        })

        when:
        String result = asciidoctor.convert(document, OptionsBuilder.options().standalone(false))

        then:
        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(result)
        htmlDocument.select('.sect1').every {
            section ->
                section.select('.paragraph').with {
                    paragraphs ->
                        !paragraphs[0].hasClass(newContentClass) &&
                                paragraphs[1].hasClass(newContentClass) &&
                                paragraphs[2].hasClass(newContentClass)
                }
        }
    }

    def "should invoke block macro processor on additionally parsed content"() {

        given:
        final List<String> contentToAdd = '''
testmacro::target[]
'''.readLines()

        final String expectedContent = 'Generated by BlockMacro'

        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            Document process(Document document) {
                document.blocks.findAll { block -> block instanceof Section }.each {
                    block ->
                        parseContent(block, contentToAdd)
                }
                document
            }
        })

        asciidoctor.javaExtensionRegistry().blockMacro(new BlockMacroProcessor('testmacro'){
            @Override
            StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
                createBlock(parent, 'paragraph', expectedContent, [:])
            }
        })

        when:
        String result = asciidoctor.convert(document, OptionsBuilder.options().standalone(false))

        then:
        result.contains(expectedContent)
    }
}
