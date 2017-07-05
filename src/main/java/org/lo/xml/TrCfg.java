package org.lo.xml;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

/**
 * Config properties of {@link TransformerFactory} and {@link Transformer}. The
 * purpose is to build a Supplier of Transformer.
 *
 * @author phuc
 */
public class TrCfg {

    public TransformerFactory factory;

    public Source xsl;

    public URIResolver resolver;

    public ErrorListener listener;

    public final Map<String, Object> params = new HashMap<>();

    public final Map<String, String> outputProps = new HashMap<>();

    @Override
    public String toString() {
        return Arrays
            .asList(factory, xsl, resolver, listener, params, outputProps)
            .toString();
    }

    public Supplier<Transformer> build() {
        return transformer(transformerFactory());
    }

    /** Create Templates given the xsl is set */
    public Templates templates() {
        return F.newTemplates(transformerFactory(), xsl);
    }

    public TrCfg factory(TransformerFactory tf) {
        this.factory = tf;
        return this;
    }

    public TrCfg xsl(Supplier<Node> node) {
        return xsl(node.get());
    }

    public TrCfg xsl(Node doc) {
        return xsl(F.toString(doc));
    }

    public TrCfg xsl(String xsl) {
        return xsl(new StreamSource(new StringReader(xsl)));
    }

    public TrCfg xsl(Source xsl) {
        this.xsl = xsl;
        return this;
    }

    public TrCfg resolver(URIResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    public TrCfg errorListener(ErrorListener listener) {
        this.listener = listener;
        return this;
    }

    public TrCfg param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public TrCfg clearParams() {
        params.clear();
        return this;
    }

    public TrCfg outputProp(String name, String value) {
        outputProps.put(name, value);
        return this;
    }

    public TrCfg version(String version) {
        outputProp(OutputKeys.VERSION, version);
        return this;
    }

    public TrCfg version(Number version) {
        return version(String.valueOf(version));
    }

    public TrCfg outputMethod(String method) {
        outputProp(OutputKeys.METHOD, method);
        return this;
    }

    public TrCfg indent(String yesno) {
        outputProp(OutputKeys.INDENT, yesno);
        return this;
    }

    public TrCfg indent(boolean indent) {
        return indent(indent ? "yes" : "no");
    }

    public TrCfg encoding(String encoding) {
        outputProp(OutputKeys.ENCODING, encoding);
        return this;
    }

    public TrCfg encoding(Charset cs) {
        return encoding(cs.name());
    }

    public TrCfg omitXmlDecl(String yesno) {
        outputProp(OutputKeys.OMIT_XML_DECLARATION, yesno);
        return this;
    }

    public TrCfg omitXmlDecl(boolean omit) {
        return omitXmlDecl(omit ? "yes" : "no");
    }

    private TransformerFactory transformerFactory() {
        TransformerFactory tf = factory == null
            ? TransformerFactory.newInstance() : factory;
        if (resolver != null) {
            tf.setURIResolver(resolver);
        }
        if (listener != null) {
            tf.setErrorListener(listener);
        }
        return tf;
    }

    private Supplier<Transformer> transformer(TransformerFactory tf) {
        if (xsl == null) {
            return () -> transformer(F.newTransformer(tf));
        } else {
            Templates tp = F.newTemplates(tf, xsl);
            return () -> transformer(F.newTransformer(tp));
        }
    }

    private Transformer transformer(Transformer tr) {
        if (resolver != null) {
            tr.setURIResolver(resolver);
        }
        if (listener != null) {
            tr.setErrorListener(listener);
        }
        params.forEach(tr::setParameter);
        outputProps.forEach(tr::setOutputProperty);
        return tr;
    }

}