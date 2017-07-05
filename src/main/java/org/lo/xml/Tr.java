package org.lo.xml;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

/**
 * Xml transformation from different types (string, node, supplier of node). The
 * transformation requires a Supplier of Transformer.
 *
 * @author phuc
 */
public class Tr {

    public final Supplier<Transformer> tr;

    public static Tr xsl(Supplier<Node> node) {
        return of(c -> c.xsl(node));
    }

    public static Tr xsl(Node node) {
        return of(c -> c.xsl(node));
    }

    public static Tr xsl(String xsl) {
        return of(c -> c.xsl(xsl));
    }

    public static Tr of(Consumer<TrCfg> c) {
        TrCfg cfg = new TrCfg();
        c.accept(cfg);
        return new Tr(cfg);
    }

    public Tr(TrCfg cfg) {
        this(cfg.build());
    }

    public Tr(Templates templates) {
        this(() -> F.newTransformer(templates));
    }

    public Tr(Supplier<Transformer> tr) {
        this.tr = F.checkNotNull(tr);
    }

    @Override
    public String toString() {
        return tr.toString();
    }

    public String apply() {
        return apply(F.newDocument());
    }

    public String apply(Supplier<Node> node) {
        return apply(node.get());
    }

    public String apply(Node node) {
        return apply(new DOMSource(node));
    }

    public String apply(Source input) {
        return F.transform(this.tr.get(), input);
    }

}