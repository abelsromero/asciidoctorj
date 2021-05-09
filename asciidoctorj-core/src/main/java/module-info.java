module org.asciidoctor.jruby {
    
    exports org.asciidoctor.jruby;
    exports org.asciidoctor.jruby.ast.impl;
    exports org.asciidoctor.jruby.cli;
    exports org.asciidoctor.jruby.converter.spi;
    exports org.asciidoctor.jruby.extension.spi;
    exports org.asciidoctor.jruby.syntaxhighlighter.spi;
    
    requires org.asciidoctor;
    requires static osgi.annotation;
}
