package org.lo.xml;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Build DOM for xslt programmatically.
 *
 * @author phuc
 */
public abstract class Xsl implements Supplier<Node> {

    /** Represent node level */
    public static final Object L = Xml.L;

    /** Prefix for xslt namespace */
    private String xslPrefix;

    /** Stack of builders; each builder captures context of a node */
    private final Stack<Xml> builders;

    /**
     * Create an empty instance and pass it to a Consumer for further actions
     */
    public static Xsl create(Consumer<Xsl> c) {
        Xsl x = create();
        c.accept(x);
        return x;
    }

    /** Create an empty instance */
    public static Xsl create() {
        return new Xsl() {
            @Override
            protected void doBuild() {}
        };
    }

    public Xsl() {
        xslPrefix = "xsl";
        builders = new Stack<>();
        builders.push(Xml.create());
    }

    protected abstract void doBuild();

    /** Build then return 'this' for chaining */
    public Xsl build() {
        doBuild();
        return this;
    }

    /** Build then return the result */
    public Node make() {
        build();
        return get();
    }

    /** Import the xsl being built into a new document */
    public Document export() {
        return builder().export();
    }

    /** Import the xsl being built into the given node */
    public <N extends Node> N export(N node) {
        return builder().export(node);
    }

    /** Get the first node added at level 1 */
    @Override
    public Node get() {
        return builder().get();
    }

    /**
     * Return the nodes created by running the given action in the context of a
     * new builder.
     */
    public List<Node> get(Runnable action) {
        builders.push(Xml.create());
        try {
            action.run();
            return builder().children();
        } finally {
            builders.pop();
        }
    }

    /** The current builder in effect */
    private Xml builder() {
        return builders.peek();
    }

    public String toXml() {
        return builder().toXml();
    }

    @Override
    public String toString() {
        return builders.toString();
    }

    /** Set xslt prefix, which defaults to "xsl" */
    public Xsl xslPrefix(String prefix) {
        this.xslPrefix = prefix;
        return this;
    }

    /** Prepend prefix to a given tag */
    public String xsl(String tag) {
        return xslPrefix + ":" + tag;
    }

    /**
     * Add nodes where strings are converted to elements. The element tags are
     * the strings themselves.
     */
    public void ae(Object... args) {
        builder().ae(args);
    }

    /**
     * Add nodes where strings are converted to text nodes.
     */
    public void at(Object... args) {
        builder().at(args);
    }

    /**
     * Add nodes where strings are converted to text nodes. A line separator is
     * appended to the end.
     */
    public void al(Object... args) {
        List<?> line = F.conj(args, t(System.lineSeparator()));
        builder().at(line.toArray());
    }

    public Element e(String tagName, Node first, Node[] more) {
        return e(tagName, F.cons(first, more));
    }

    public Element e(String tagName, Node first, Node second, Node[] more) {
        return e(tagName, F.cons(first, second, more));
    }

    public Element e(String tagName, Node... children) {
        return e(tagName, asList(children));
    }

    public Element e(String tagName, Iterable<? extends Node> children) {
        return builder().e(tagName, children);
    }

    public Text t(Object text) {
        return builder().t(text);
    }

    public Comment c(Object o) {
        return builder().c(o);
    }

    public Attr id(Object id) {
        return builder().id(id);
    }

    public Attr a(String name, Object value) {
        return builder().a(name, value);
    }

    /**
     * The xsl:stylesheet and xsl:transform elements are synonymous elements.
     * Both are used to define the root element of the style sheet.
     */
    public Element xslTransform(Node... children) {
        return e(xsl("transform"), xmlnsXsl(), children);
    }

    /**
     * Defines the root element of the style sheet.
     */
    public Element xslStyleSheet(Node... children) {
        return e(xsl("stylesheet"), xmlnsXsl(), children);
    }

    public Element xslStyleSheetV1(Node... children) {
        return xslStyleSheetVersion1(children);
    }

    public Element xslStyleSheetVersion1(Node... children) {
        return e(xsl("stylesheet"), a("version", "1.0"), xmlnsXsl(), children);
    }

    public Element xslStyleSheetV2(Node... children) {
        return xslStyleSheetVersion2(children);
    }

    public Element xslStyleSheetVersion2(Node... children) {
        return e(xsl("stylesheet"), a("version", "2.0"), xmlnsXsl(), children);
    }

    public Element xslStyleSheetV3(Node... children) {
        return xslStyleSheetVersion3(children);
    }

    public Element xslStyleSheetVersion3(Node... children) {
        return e(xsl("stylesheet"), a("version", "3.0"), xmlnsXsl(), children);
    }

    /** @see #xsOutput(Attr...) */
    public Element xsOutputXml(Attr... attrs) {
        return xsOutput(F.cons(a("method", "xml"), attrs));
    }

    /** @see #xsOutput(Attr...) */
    public Element xsOutputHtml(Attr... attrs) {
        return xsOutput(F.cons(a("method", "html"), attrs));
    }

    /** @see #xsOutput(Attr...) */
    public Element xsOutputText(Attr... attrs) {
        return xsOutput(F.cons(a("method", "text"), attrs));
    }

    /**
     * Define format of the output document. All attributes are optional:
     * method, version, encoding, omit-xml-declaration, standalone,
     * doctype-public, doctype-system, cdata-section-elements, indent,
     * media-type.
     */
    public Element xsOutput(Attr... attrs) {
        return e(xsl("output"), attrs);
    }

    public Element xsOutput(Iterable<Attr> attrs) {
        return e(xsl("output"), attrs);
    }

    /**
     * Trim whitespaces-only text nodes
     */
    public Element xsStripSpace(String namelist) {
        return e(xsl("strip-space"), a("elements", namelist));
    }

    /**
     * Preserve whitespaces-only text nodes. This is default setting and is only
     * needed to override strip-space.
     */
    public Element xsPreserveSpace(String elements) {
        return e(xsl("preserve-space"), a("elements", elements));
    }

    public Element xsPI(String name, Node... children) {
        return xsProcessingInstruction(name, children);
    }

    public Element xsPI(String name, String data) {
        return xsProcessingInstruction(name, data);
    }

    public Element xsProcessingInstruction(String name, Node... children) {
        return e(xsl("processing-instruction"), a("name", name), children);
    }

    public Element xsProcessingInstruction(String name, String data) {
        return xsProcessingInstruction(name, t(data));
    }

    /** Value of current node */
    public Element xsValue() {
        return xsValueOf();
    }

    /** Extract values of selected nodes */
    public Element xsValue(String select, Attr... attrs) {
        return xsValueOf(select, attrs);
    }

    /** Value of current node */
    public Element xsValueOf() {
        return xsValueOf(".");
    }

    /**
     * Extract value of selected nodes. Attributes: select |
     * disable-output-escaping.
     */
    public Element xsValueOf(String select, Attr... attrs) {
        return e(xsl("value-of"), a("select", select), attrs);
    }

    public Element xsTemplate(String match, Node... children) {
        return e(xsl("template"), a("match", match), children);
    }

    /**
     * xsl:template defines rules to apply when certain nodes are matched.
     * <p>
     * xsl:template name="name" match="pattern" mode="mode" priority="number"
     */
    public Element xsTemplate(Node... children) {
        return e(xsl("template"), children);
    }

    public Element xsApply(String select, String mode, Node... children) {
        return xsApplyTemplates(select, mode, children);
    }

    public Element xsApply(String select, Node... children) {
        return xsApplyTemplates(select, children);
    }

    public Element xsApply(Node first, Node[] more) {
        return xsApplyTemplates(first, more);
    }

    public Element xsApply(Node... children) {
        return xsApplyTemplates(children);
    }

    public Element xsApplyTemplates(String select, String mode,
        Node... children) {
        return xsApplyTemplates(
            F.cons(a("select", select), a("mode", mode), children));
    }

    public Element xsApplyTemplates(String select, Node... children) {
        return xsApplyTemplates(a("select", select), children);
    }

    public Element xsApplyTemplates(Node first, Node[] more) {
        return xsApplyTemplates(F.cons(first, more));
    }

    public Element xsApplyTemplates(Node... children) {
        return xsApplyTemplates(asList(children));
    }

    /**
     * Apply the highest-priority template for each selected node. Attributes:
     * select (optional), mode (optional).
     * <p>
     * The mode attribute is used to give priority to templates that specifies
     * the mode.
     */
    public Element xsApplyTemplates(Iterable<? extends Node> children) {
        return e(xsl("apply-templates"), children);
    }

    /**
     * Apply a named-template.
     */
    public Element xsCall(String name, Node... children) {
        return e(xsl("call-template"), a("name", name), children);
    }

    /**
     * Apply a named-template.
     */
    public Element xsCallTemplate(String name, Node... children) {
        return e(xsl("call-template"), a("name", name), children);
    }

    /**
     * Make shallow copy, i.e. copy with no attributes and no children.
     * Attributes: use-attribute-sets (optional)
     */
    public Element xsCopy(Node... children) {
        return e(xsl("copy"), children);
    }

    /** Make deep copy of selected nodes */
    public Element xsCopyOf(String select, Node... children) {
        return e(xsl("copy-of"), a("select", select), children);
    }

    public Element xsNumber(String format, Attr... attrs) {
        return e(xsl("number"), a("format", format), attrs);
    }

    /**
     * xsl:number is for formatting number.
     * <p>
     * xsl:number count="expression" level="single|multiple|any"
     * from="expression" value="expression" format="formatstring"
     * lang="languagecode" letter-value="alphabetic|traditional"
     * grouping-separator="character" grouping-size="number"
     */
    public Element xsNumber(Attr... attrs) {
        return e(xsl("number"), attrs);
    }

    /**
     * Defines the characters and symbols to be used when converting numbers
     * into strings, with the format-number() function.
     * <p>
     * xsl:decimal-format name="name" decimal-separator="char"
     * grouping-separator="char" infinity="string" minus-sign="char" NaN=
     * "string" percent="char" per-mille="char" zero-digit="char" digit="char"
     * pattern-separator="char"
     */
    public Element xsDecimalFormat(Attr... attrs) {
        return e(xsl("decimal-format"), attrs);
    }

    public Element xsText(String text, Node... children) {
        return e(xsl("text"), F.cons(t(text), children));
    }

    public Element xsText(Node... children) {
        return e(xsl("text"), children);
    }

    public Element xsText(Iterable<Node> children) {
        return e(xsl("text"), children);
    }

    public Element xsLine() {
        return xsLine("");
    }

    public Element xsLine(String text) {
        return xsText(text + System.lineSeparator());
    }

    public Element xsComment(String comment) {
        return e(xsl("comment"), t(comment));
    }

    public Element xsElement(String name, Node... children) {
        return e(xsl("element"), F.cons(a("name", name), children));
    }

    public Element xsEle(String name, Node... children) {
        return xsElement(name, children);
    }

    public Element xsAttribute(String name, Node... children) {
        return e(xsl("attribute"), F.cons(a("name", name), children));
    }

    public Element xsAttr(String name, Node... children) {
        return xsAttribute(name, children);
    }

    public Element xsAttrSet(String name, Node... children) {
        return xsAttributeSet(name, children);
    }

    public Element xsAttributeSet(String name, Node... children) {
        return e(xsl("attribute-set"), F.cons(a("name", name), children));
    }

    public Element xsVariable(String name, String select) {
        return e(xsl("variable"), a("name", name), a("select", select));
    }

    public Element xsVariable(String name, Node... children) {
        return e(xsl("variable"), F.cons(a("name", name), children));
    }

    public Element xsParam(String name, String select) {
        return e(xsl("param"), a("name", name), a("select", select));
    }

    public Element xsParam(String name, Node... children) {
        return e(xsl("param"), F.cons(a("name", name), children));
    }

    public Element xsWithParam(String name, String select) {
        return e(xsl("with-param"), a("name", name), a("select", select));
    }

    public Element xsWithParam(String name, Node... children) {
        return e(xsl("with-param"), a("name", name));
    }

    public Element xsSort(String select, Attr... attrs) {
        return e(xsl("sort"), a("select", select), attrs);
    }

    /**
     * Attributes: select, lang, data-type, order and case-order. All attributes
     * are optional.
     */
    public Element xsSort(Attr... attrs) {
        return e(xsl("sort"), attrs);
    }

    /**
     * Loop through nodes specified by the expression.
     */
    public Element xsForEach(String expression, Node... children) {
        return e(xsl("for-each"), a("select", expression), children);
    }

    /**
     * Declares a named key to be used with key() function.
     *
     * @param name = name of key
     * @param match = nodes to which key is applied
     * @param use = value of the key for each node
     */
    public Element xsKey(String name, String match, String use) {
        return e(xsl("key"), a("name", name), a("match", match), a("use", use));
    }

    public Element xsIf(String test, Node... children) {
        return e(xsl("if"), a("test", test), children);
    }

    /**
     * The xsl:choose element is used in conjunction with xsl:when and
     * xsl:otherwise to express multiple conditional tests.
     */
    public Element xsChoose(Node... children) {
        return e(xsl("choose"), children);
    }

    public Element xsWhen(String test, Node... children) {
        return e(xsl("when"), a("test", test), children);
    }

    public Element xsOtherwise(Node... children) {
        return e(xsl("otherwise"), children);
    }

    /**
     * The xsl:import element imports the contents of one style sheet into
     * another. An imported style sheet has lower precedence than the importing
     * style sheet.
     */
    public Element xsImport(String href, Node... children) {
        return e(xsl("import"), a("href", href), children);
    }

    /**
     * By default, template rules in imported style sheets have lower precedence
     * than template rules in main style sheets. The xsl:apply-imports element
     * applies a template rule from an imported style sheet.
     */
    public Element xsApplyImports(Node... children) {
        return e(xsl("apply-imports"), children);
    }

    /**
     * The xsl:include element includes the contents of one style sheet into
     * another. An included style sheet has the same precedence as the including
     * style sheet.
     */
    public Element xsInclude(String href, Node... children) {
        return e(xsl("include"), a("href", href), children);
    }

    /**
     * The xsl:fallback element specifies alternate code to run if the XSL
     * processor does not support an xsl element.
     */
    public Element xsFallback(Node... children) {
        return e(xsl("fallback"), children);
    }

    /**
     * The xsl:fallback element specifies alternate code to run if the XSL
     * processor does not support an xsl element.
     */
    public Element xsMessage(Node... children) {
        return e(xsl("message"), children);
    }

    /**
     * The xsl:namespace-alias element replaces a namespace in the style sheet
     * to a different namespace in the output.
     */
    public Element xsNamespaceAlias(String name, String renamed) {
        return e(xsl("namespace-alias"), a("stylesheet-prefix", name),
            a("result-prefix", renamed));
    }

    public Attr terminate(String yesno) {
        return a("terminate", yesno);
    }

    public Attr terminate(boolean terminate) {
        return a("terminate", terminate ? "yes" : "no");
    }

    public Attr terminate() {
        return a("terminate", "yes");
    }

    public Attr noterminate() {
        return a("terminate", "no");
    }

    public Attr method(String method) {
        return a("method", method);
    }

    public Attr indent() {
        return indent("yes");
    }

    public Attr noindent() {
        return indent("no");
    }

    public Attr indent(boolean indent) {
        return indent ? indent("yes") : indent("no");
    }

    public Attr indent(String indent) {
        return a("indent", indent);
    }

    public Attr omitXmlDecl() {
        return omitXmlDeclaration("yes");
    }

    public Attr xmlDecl() {
        return omitXmlDeclaration("no");
    }

    public Attr omitXmlDecl(boolean omit) {
        return omitXmlDecl(omit ? "yes" : "no");
    }

    public Attr omitXmlDecl(String yesno) {
        return omitXmlDeclaration(yesno);
    }

    public Attr omitXmlDeclaration(String yesno) {
        return a("omit-xml-declaration", yesno);
    }

    public Attr doctypeSystem(String value) {
        return a("doctype-system", value);
    }

    public Attr doctypePublic(String value) {
        return a("doctype-public", value);
    }

    /** @see #cdataSectionElements(String) */
    public Attr cdataElements(String namelist) {
        return cdataSectionElements(namelist);
    }

    public Attr cdataSectionElements(String namelist) {
        return a("cdata-section-elements", namelist);
    }

    public Attr useAttrSet(String name) {
        return useAttributeSet(name);
    }

    public Attr useAttributeSet(String name) {
        return a("use-attribute-sets", name);
    }

    public Attr escape() {
        return disableOutputEscaping("no");
    }

    public Attr noescape() {
        return disableOutputEscaping("yes");
    }

    public Attr disableOutputEscaping(String yesno) {
        return a("disable-output-escaping", yesno);
    }

    public Attr name(String name) {
        return a("name", name);
    }

    public Attr match(String expression) {
        return a("match", expression);
    }

    public Attr priority(String priority) {
        return a("priority", priority);
    }

    public Attr priority(Number priority) {
        return priority(String.valueOf(priority));
    }

    /**
     * Mode pumps up matching priority for templates that have the mode.
     */
    public Attr mode(String mode) {
        return a("mode", mode);
    }

    /** order = ascending | descending */
    public Attr order(String order) {
        return a("order", order);
    }

    public Attr orderAsending() {
        return a("order", "ascending");
    }

    public Attr orderDescending() {
        return a("order", "descending");
    }

    /** case-order = upper-first | lower-first */
    public Attr caseOrder(String order) {
        return a("case-order", order);
    }

    public Attr caseOrderUpper() {
        return a("case-order", "upper-first");
    }

    public Attr caseOrderLower() {
        return a("case-order", "lower-first");
    }

    public Attr count(String expression) {
        return a("count", expression);
    }

    /** data-type = text | number | qname */
    public Attr dataType(String dt) {
        return a("data-type", dt);
    }

    public Attr dataTypeText() {
        return a("data-type", "text");
    }

    public Attr dataTypeNumber() {
        return a("data-type", "number");
    }

    public Attr dataTypeQName() {
        return a("data-type", "qname");
    }

    /** Specifies the decimal point character. Default is "." */
    public Attr decimalSep(String sep) {
        return decimalSeparator(sep);
    }

    /** Specifies the decimal point character. Default is "." */
    public Attr decimalSeparator(String sep) {
        return a("decimal-separator", sep);
    }

    public Attr encoding(String encoding) {
        return a("encoding", encoding);
    }

    public Attr encoding(Charset encoding) {
        return encoding(encoding.name());
    }

    public Attr excludeResultPrefixes(String first, String... more) {
        String value = F.cons(first, more).stream()
            .collect(joining(" "));
        return a("exclude-result-prefixes", value);
    }

    public Attr extensionElementPrefixes(String first, String... more) {
        String value = F.cons(first, more).stream()
            .collect(joining(" "));
        return a("extension-element-prefixes", value);
    }

    /** An expression to start numering from */
    public Attr from(String expression) {
        return a("from", expression);
    }

    public Attr lang(String lang) {
        return a("lang", lang);
    }

    /** level = single | multiple | any */
    public Attr level(String level) {
        return a("level", level);
    }

    public Attr levelSingle() {
        return a("level", "single");
    }

    public Attr levelMultiple() {
        return a("level", "multiple");
    }

    public Attr levelAny() {
        return a("level", "any");
    }

    public Attr value(Object expression) {
        return a("value", expression);
    }

    public Attr format(String format) {
        return a("format", format);
    }

    /** value = alphabetic | traditional */
    public Attr letterValue(String value) {
        return a("letter-value", value);
    }

    public Attr letterAlphabetic() {
        return a("letter-value", "alphabetic");
    }

    public Attr letterTraditional() {
        return a("letter-value", "traditional");
    }

    public Attr groupingSeparator(String sep) {
        return a("grouping-separator", sep);
    }

    public Attr groupSep(String sep) {
        return a("grouping-separator", sep);
    }

    public Attr groupingSize(String size) {
        return a("grouping-size", size);
    }

    public Attr groupingSize(Integer size) {
        return a("grouping-size", size);
    }

    public Attr groupSize(String size) {
        return a("grouping-size", size);
    }

    public Attr groupSize(Integer size) {
        return a("grouping-size", size);
    }

    public Attr version(String version) {
        return a("version", version);
    }

    public Attr version(Number version) {
        return a("version", version);
    }

    public Attr test(String expression) {
        return a("test", expression);
    }

    public Attr href(String href) {
        return a("href", href);
    }

    /** xsl:name=value */
    public Attr xsA(String name, Object value) {
        return a(xsl(name), value);
    }

    /** xsl:version */
    public Attr xsVersion(String version) {
        return a(xsl("version"), version);
    }

    /** xsl:version */
    public Attr xsVersion(Number version) {
        return a(xsl("version"), version);
    }

    public Attr stylesheetPrefix(String value) {
        return a("stylesheet-prefix", value);
    }

    public Attr resultPrefix(String value) {
        return a("result-prefix", value);
    }

    public Element xsNameSpaceAlias(Attr... children) {
        return e(xsl("namespace-alias"));
    }

    /** xmlns:xsl="http://www.w3.org/1999/XSL/Transform" */
    public Attr xmlnsXsl() {
        return a("xmlns:" + xslPrefix, "http://www.w3.org/1999/XSL/Transform");
    }

    /** xmlns="http://www.w3.org/1999/xhtml" */
    public Attr xmlnsXhtml() {
        return a("xmlns", "http://www.w3.org/1999/xhtml");
    }

}