package org.lo.xml;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * Build xml DOM Node in a visual way.
 *
 * @author phuc
 */
public abstract class Xml implements Supplier<Node> {

    /** Represent node level */
    public static final Object L = TreeBuilder.L;

    /** Require an owner document to create nodes */
    public final Document owner;

    /**
     * Parse arguments to nodes and add nodes to their parents. If child is
     * attribute, it is added to parent as attribute node.
     */
    private final TreeBuilder<Node> builder = new TreeBuilder<Node>() {
        @Override
        protected void addChild(Node parent, Node child) {
            if (parent instanceof Element && child instanceof Attr) {
                ((Element) parent).setAttributeNode((Attr) child);
            } else {
                parent.appendChild(child);
            }
        }

        @Override
        protected Collection<Node> parseArguments(Iterable<?> args) {
            List<Node> nodes = new ArrayList<>();
            for (Object arg : args) {
                if (arg instanceof Node) {
                    nodes.add(importOne((Node) arg));
                } else if (arg instanceof Object[]) {
                    List<Object> ls = asList((Object[]) arg);
                    nodes.addAll(parseArguments(ls));
                } else if (arg instanceof Iterable) {
                    for (Object a : (Iterable<?>) arg) {
                        for (Node child : parseArguments(singleton(a))) {
                            nodes.add(child);
                        }
                    }
                }
            }
            return nodes;
        }
    };

    /**
     * Create an instance with empty {@link #doBuild()}.
     */
    public static Xml create() {
        return new Xml() {
            @Override
            protected void doBuild() {}
        };
    }

    /**
     * Create an empty instance then initialize it with its Consumer.
     */
    public static Xml create(Consumer<Xml> c) {
        Xml b = create();
        c.accept(b);
        return b;
    }

    /**
     * Create an instance with an initial node given the node's tag and text.
     */
    public static Xml create(String tag, Object text) {
        return new Xml() {
            @Override
            protected void doBuild() {
                ae(e(tag, t(text)));
            }
        }.build();
    }

    /**
     * Instantiate with a new owner document
     */
    public Xml() {
        this.owner = F.newDocument();
    }

    /**
     * Instantiate given the document node
     */
    public Xml(Document doc) {
        this.owner = doc;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public String toXml() {
        return F.toString(get());
    }

    /** Implement to build the node. */
    protected abstract void doBuild();

    /**
     * Call {@link #doBuild()} and return 'this' for chaining. Can be overridden
     * to add nodes before/after the main {@link #doBuild()}.
     */
    public Xml build() {
        doBuild();
        return this;
    }

    /**
     * Import nodes being built into a new document. Return the document.
     */
    public Document export() {
        return export(F.newDocument());
    }

    /**
     * Import nodes being built into the given node. Return the given node.
     */
    public <N extends Node> N export(N node) {
        Document doc;
        if (node instanceof Document) {
            doc = (Document) node;
        } else {
            doc = node.getOwnerDocument();
        }
        children().stream().map(c -> F.tryImportNode(doc, c))
            .forEach(c -> builder.addChild(node, c));
        return node;
    }

    public List<Node> importAll(Iterable<? extends Node> nodes) {
        List<Node> adopted = new ArrayList<>();
        for (Node n : nodes) {
            n = importOne(n);
            adopted.add(n);
        }
        return adopted;
    }

    public List<Node> importAll(Node... nodes) {
        return importAll(asList(nodes));
    }

    public Node importOne(Node node) {
        return F.tryImportNode(owner, node);
    }

    public Node make() {
        return build().get();
    }

    @Override
    public Node get() {
        return builder.get();
    }

    public List<Node> children() {
        return builder.children();
    }

    public <T extends Node> T get(Class<T> type) {
        return type.cast(get());
    }

    public void add(Iterable<?> args) {
        builder.add(args);
    }

    /** Add nodes where CharSequence are converted to elements */
    public void ae(Object... args) {
        List<Object> ls = stream(args)
            .map(a -> a instanceof CharSequence ? e(String.valueOf(a)) : a)
            .collect(toList());
        builder.add(ls);
    }

    /** Add nodes where CharSequence are converted to text nodes */
    public void at(Object... args) {
        List<Object> ls = stream(args)
            .map(a -> a instanceof CharSequence ? t(a) : a)
            .collect(toList());
        builder.add(ls);
    }

    /** Add nodes where CharSequence are converted to comment nodes */
    public void ac(Object... args) {
        List<Object> ls = stream(args)
            .map(a -> a instanceof CharSequence ? c(a) : a)
            .collect(toList());
        builder.add(ls);
    }

    public Element e(String tagName, String text, Node... children) {
        return e(tagName, F.cons(t(text), children));
    }

    public Element e(String tagName, Node... children) {
        return e(tagName, asList(children));
    }

    public Element e(String tagName, Iterable<? extends Node> children) {
        Element ele = owner.createElement(tagName);
        importAll(children).forEach(child -> builder.addChild(ele, child));
        return ele;
    }

    public Attr a(String name, Object value) {
        Attr attr = owner.createAttribute(name);
        attr.setValue(String.valueOf(value));
        return attr;
    }

    public Text t(Object data) {
        return owner.createTextNode(valueOf(data));
    }

    public Comment c(Object data) {
        return owner.createComment(valueOf(data));
    }

    public ProcessingInstruction pi(
        String target,
        String data,
        Node... children) {
        ProcessingInstruction pi;
        pi = owner.createProcessingInstruction(target, data);
        importAll(children).forEach(child -> builder.addChild(pi, child));
        return pi;
    }

    public CDATASection cdata(Object data) {
        return owner.createCDATASection(valueOf(data));
    }

    public DocumentType dt(
        String qualifiedName,
        String publicId,
        String systemId) {
        DOMImplementation dom = owner.getImplementation();
        DocumentType doctype;
        doctype = dom.createDocumentType(qualifiedName, publicId, systemId);
        owner.appendChild(doctype);
        return doctype;
    }

    public Attr id(Object id) {
        return a("id", id);
    }

    public Attr name(Object name) {
        return a("name", name);
    }

}
