package org.lo.xml;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

/**
 * Base functions/methods/constants.
 *
 * @author phuc
 */
public interface F {

    public static Transformer newTransformer(Templates templates) {
        try {
            return templates.newTransformer();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Make identity-transfomer */
    public static Transformer newTransformer(TransformerFactory factory) {
        try {
            return factory.newTransformer();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Make transformer from xslt source */
    public static Transformer newTransformer(TransformerFactory tf,
        Source xsl) {
        try {
            return tf.newTransformer(xsl);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Templates newTemplates(TransformerFactory tf, Source xsl) {
        try {
            return tf.newTemplates(xsl);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Document newDocument() {
        try {
            DocumentBuilderFactory factory;
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new Error("Should not happen", e);
        }
    }

    /**
     * If possible, import a node into a document and return the imported node.
     * Otherwise, return the source node.
     */
    public static Node tryImportNode(Document doc, Node node) {
        if (node instanceof DocumentType) {
            return node;
        } else if (node instanceof Document) {
            return node;
        } else if (node.getOwnerDocument() == doc) {
            return node;
        } else {
            return doc.importNode(node, true);
        }
    }

    public static Object eval(XPath xp, String expr, Node node, QName type) {
        try {
            return xp.evaluate(expr, node, type);
        } catch (XPathExpressionException e) {
            String msg = "Failed to eval %s in %s";
            msg = String.format(msg, expr, node);
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * Convert node to xml string with no indent and no xml declaration.
     */
    public static String toString(Node node) {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer tr;
        try {
            tr = factory.newTransformer();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return transform(tr, new DOMSource(node));
    }

    public static String transform(Transformer tr, Source input) {
        StringWriter output = new StringWriter();
        try {
            tr.transform(input, new StreamResult(output));
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        }
        return output.toString();
    }

    public static String removeLineBreaks(String s) {
        BufferedReader reader = new BufferedReader(new StringReader(s));
        return reader.lines().collect(Collectors.joining());
    }

    public static String join(Object... data) {
        return Arrays.stream(data).map(String::valueOf)
            .collect(Collectors.joining());
    }

    public static String join(Object first, Object[] more) {
        return join(cons(first, more));
    }

    public static String join(Iterable<?> items) {
        return stream(items).map(String::valueOf).collect(Collectors.joining());
    }

    public static <T> Stream<T> stream(Iterable<T> items) {
        return StreamSupport.stream(items.spliterator(), false);
    }

    public static <T> List<T> toList(Iterable<T> items) {
        return stream(items).collect(Collectors.toList());
    }

    /** Join the first and the rest items. */
    public static <T> List<T> cons(T first, T[] rest) {
        List<T> cons = new ArrayList<>(rest.length + 1);
        cons.add(first);
        cons.addAll(Arrays.asList(rest));
        return cons;
    }

    /** Join the first, second and rest items. */
    public static <T> List<T> cons(T first, T second, T[] rest) {
        List<T> cons = new ArrayList<>(rest.length + 2);
        cons.add(first);
        cons.add(second);
        cons.addAll(Arrays.asList(rest));
        return cons;
    }

    /** Join the first and last items */
    public static <T> List<T> conj(T[] first, T last) {
        List<T> cons = new ArrayList<>(first.length + 1);
        cons.addAll(Arrays.asList(first));
        cons.add(last);
        return cons;
    }

    public static void println(Object first, Object... more) {
        StringBuilder sb = new StringBuilder().append(first);
        Arrays.stream(more).forEach(sb::append);
        System.out.println(sb);
    }

    public static <T> T checkNotNull(T o) {
        if (o == null) {
            throw new NullPointerException();
        } else {
            return o;
        }
    }

}