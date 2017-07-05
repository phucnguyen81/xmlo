package org.lo.xml.xsl;

import static org.junit.Assert.*;

import org.junit.Test;

import org.lo.xml.F;
import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

public class OutputTest {

    /**
     * Generate code from xml. The code is mostly text with xml data inserted as
     * parameters.
     */
    @Test
    public void outputText2() {
        Xml name = new Xml() {
            @Override
            protected void doBuild() {
                ae("name");
                ae(L, e("last", t("Churchill")));
                ae(L, e("first", t("Winston")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("name"));
                ae(L, L, get(this::name));
            }

            private void name() {
                al("using System");
                al("using System.Xml");
                al();
                al("class Name");
                al();
                al("  static void Main() {");
                al("    XmlTextWriter w = new XmlTextWriter(Console.Out);");
                al("    w.Formatting = Formatting.Indented;");
                al("    w.Indentation = 1;");
                al("    w.writeStartDocument();");
                al("    w.WriteStartElement(\"", xsValueOf("name()"), "\")");
                al("    w.WriteElementString(\"family\", \"", xsValueOf("last"),
                    "\")");
                al("    w.WriteElementString(\"given\", \"", xsValueOf("first"),
                    "\")");
                al("    w.writeEndElement();");
                al("    w.close();");
                al("  }");
                al("}");
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("using System");
                l("using System.Xml");
                l();
                l("class Name");
                l();
                l("  static void Main() {");
                l("    XmlTextWriter w = new XmlTextWriter(Console.Out);");
                l("    w.Formatting = Formatting.Indented;");
                l("    w.Indentation = 1;");
                l("    w.writeStartDocument();");
                l("    w.WriteStartElement(\"name\")");
                l("    w.WriteElementString(\"family\", \"Churchill\")");
                l("    w.WriteElementString(\"given\", \"Winston\")");
                l("    w.writeEndElement();");
                l("    w.close();");
                l("  }");
                l("}");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(name));
    }

    @Test
    public void outputText() {
        Xml message = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("message", t("Hello World!")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("message", t("Message: "), xsApply()));
            }
        }.build();

        String text = Tr.xsl(xsl).apply(message);
        assertEquals("Message: Hello World!", text);
    }

    /**
     * It seems there is no support for html5-doctype output. A workaround is to
     * generate the html doctype as a literal text node.
     */
    @Test
    public void outputHtml5() {
        Xml name = new Xml() {
            @Override
            protected void doBuild() {
                ae("name");
                ae(L, e("last", t("Churchill")));
                ae(L, e("first", t("Winston")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputHtml(indent(false))));
                ae(L, xsTemplate("name"));
                ae(L, L, xsText("<!DOCTYPE html>", noescape()));
                ae(L, L, "html");
                ae(L, L, L, "body");
                ae(L, L, L, L, e("p", xsApply("last")));
                ae(L, L, L, L, e("p", xsApply("first")));
            }
        }.build();

        String expected = new Str() {
            @Override
            protected void doBuild() {
                a("<!DOCTYPE html>");
                a("<html>");
                a(L, "<body>");
                a(L, L, "<p>Churchill</p>");
                a(L, L, "<p>Winston</p>");
                a(L, "</body>");
                a("</html>");
            }
        }.make();

        String actual = Tr.xsl(xsl).apply(name);
        assertEquals(expected, F.removeLineBreaks(actual));
    }

    @Test
    public void outputHtml() {
        Xml name = new Xml() {
            @Override
            protected void doBuild() {
                ae("name");
                ae(L, e("last", t("Churchill")));
                ae(L, e("first", t("Winston")));
            }
        }.build();

        String htmlsystem = "http://www.w3.org/TR/html4/strict.dtd";
        String htmlpublic = "-//W3C//DTD HTML 4.01//EN";

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputHtml(indent(false))));
                ae(L, xsOutput(a("doctype-system", htmlsystem)));
                ae(L, xsOutput(a("doctype-public", htmlpublic)));
                ae(L, xsTemplate("name"));
                ae(L, L, "html");
                ae(L, L, L, "body");
                ae(L, L, L, L, e("p", xsApply("last")));
                ae(L, L, L, L, e("p", xsApply("first")));
            }
        }.build();

        String doctype = Str.of("<!DOCTYPE html PUBLIC \"%s\" \"%s\">")
            .format(htmlpublic, htmlsystem);

        String expected = new Str() {
            @Override
            protected void doBuild() {
                a(doctype);
                a("<html>");
                a(L, "<body>");
                a(L, L, "<p>Churchill</p>");
                a(L, L, "<p>Winston</p>");
                a(L, "</body>");
                a("</html>");
            }
        }.make();

        String actual = Tr.xsl(xsl).apply(name);
        assertEquals(expected, F.removeLineBreaks(actual));
    }

}
