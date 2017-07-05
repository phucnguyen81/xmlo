package org.lo.xml.xsl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Element;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

public class XsltTest {

    @Test
    public void copy() {
        Xml eu = new Xml() {
            @Override
            protected void doBuild() {
                ae("eu");
                ae(L, "member");
                ae(L, L, e("state", t("Austria")));
                ae(L, L, e("state", a("founding", "yes"), t("Belgium")));
                ae(L, L, e("state", t("Denmark")));
                ae(L, L, e("state", a("founding", "yes"), t("France")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1());
                ae(L, xsOutputXml(noindent(), omitXmlDecl()));

                ae(L, xsTemplate("eu"));
                ae(L, L, xsApply("member"));

                ae(L, xsTemplate("member"));
                ae(L, L, "eu-members");
                ae(L, L, L, xsApply("state[@founding]"));

                ae(L, xsTemplate("state"));
                ae(L, L, xsCopy());
                ae(L, L, L, xsApply());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<eu-members>");
                a(L, "<state>Belgium</state>");
                a(L, "<state>France</state>");
                a("</eu-members>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(eu));
    }

    @Test
    public void mode() {
        Xml nums = new Xml() {
            @Override
            protected void doBuild() {
                ae("nums");
                ae(L, e("num", t("01")));
                ae(L, e("num", t("02")));
                ae(L, e("num", t("03")));
                ae(L, e("num", t("04")));
                ae(L, e("num", t("05")));
                ae(L, e("num", t("06")));
                ae(L, e("num", t("07")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            String mode = "copy";

            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl(), noindent())));
                ae(L, xsStripSpace("*"));

                ae(L, xsTemplate("num[position() mod 3 = 1]"));
                ae(L, L, "tr");
                ae(L, L, L, thisAndTwoSiblings());

                ae(L, xsTemplate("*", mode(mode)));
                ae(L, L, "td");
                ae(L, L, L, xsValueOf("."));

                ae(L, xsTemplate("num"));
            }

            Element thisAndTwoSiblings() {
                String nextTwoSiblings = "following-sibling::*[not(position() >2)]";
                return xsApply(". | " + nextTwoSiblings, mode(mode));
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<tr>");
                a(L, "<td>01</td><td>02</td><td>03</td>");
                a("</tr>");
                a("<tr>");
                a(L, "<td>04</td><td>05</td><td>06</td>");
                a("</tr>");
                a("<tr>");
                a(L, "<td>07</td>");
                a("</tr>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(nums));
    }

    @Test
    public void cdata() {
        Xml items = new Xml() {
            @Override
            protected void doBuild() {
                ae("items");
                ae(L, e("item1", t("egg & bacon")));
                ae(L, e("item2", t("salad & tomatoes")));
                ae(L, e("item3", t("cake & powder")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(indent(false), omitXmlDecl())));
                ae(L, xsOutput(cdataElements("item1 item2")));
                ae(L, xsTemplate("items"));
                ae(L, L, "items");
                ae(L, L, L, e("item1", xsApply("item1")));
                ae(L, L, L, e("item2", xsApply("item2")));
                ae(L, L, L, e("item3", xsApply("item3")));
            }
        }.build();

        String expected = new Str() {
            @Override
            protected void doBuild() {
                a("<items>");
                a(L, "<item1><![CDATA[egg & bacon]]></item1>");
                a(L, "<item2><![CDATA[salad & tomatoes]]></item2>");
                a(L, "<item3>cake &amp; powder</item3>");
                a("</items>");
            }
        }.make();

        assertEquals(expected, Tr.xsl(xsl).apply(items));
    }

    @Test
    public void noindent() {
        Xsl xslt = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputHtml(indent("no"))));
                ae(L, xsTemplate("/"));
                ae(L, L, "html");
                ae(L, L, L, "body");
                ae(L, L, L, L, e("p", t("Hello!")));
                ae(L, L, L, L, e("p", t("Hey!")));
            }
        }.build();

        String expected = "<html><body><p>Hello!</p><p>Hey!</p></body></html>";

        assertEquals(expected, Tr.xsl(xslt).apply());
    }

    @Test
    public void processingInstruction() {
        String piData = "href=\"processing.css\" type=\"text/css\"";

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("/"));
                ae(L, L, xsPI("xml-stylesheet", piData));
            }
        }.build();

        String expected = String.format("<?xml-stylesheet %s?>", piData);

        assertEquals(expected, Tr.xsl(xsl).apply());
    }

    @Test
    public void comment() {
        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("/"));
                ae(L, L, xsComment("comment & msg element"));
                ae(L, L, e("msg"));
            }
        }.build();

        String expected = "<!--comment & msg element--><msg/>";

        assertEquals(expected, Tr.xsl(xsl).apply());
    }

    @Test
    public void attributeSet() {
        Xsl xslt = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsAttrSet("paragraph"));
                ae(L, L, xsAttribute("priority", t("medium")));
                ae(L, L, xsAttribute("date", t("2003-09-23")));
                ae(L, xsTemplate("/"));
                ae(L, L, xsElement("paragraph"));
                ae(L, L, L, useAttrSet("paragraph"));
                ae(L, L, L, xsApply());
            }
        }.build();

        String expected = "<paragraph priority=\"medium\" date=\"2003-09-23\"/>";

        assertEquals(expected, Tr.xsl(xslt).apply());
    }

    @Test
    public void attribute() {
        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("/"));
                ae(L, L, xsEle("paragraph"));
                ae(L, L, L, xsAttr("priority", t("medium")));
            }
        }.build();

        String expected = "<paragraph priority=\"medium\"/>";

        assertEquals(expected, Tr.xsl(xsl).apply());
    }

    @Test
    public void element() {
        Xml msg = Xml.create("message", "Hello World!");

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("message"));
                ae(L, L, xsElement("{concat('my', name())}"));
                ae(L, L, L, xsApply());
            }
        }.build();

        String expected = "<mymessage>Hello World!</mymessage>";

        assertEquals(expected, Tr.xsl(xsl).apply(msg));
    }

    @Test
    public void attributeValueTemplate() {
        Xml doc = Xml.create(b -> {
            b.ae(b.e("doc", b.a("styletype", "text/css")));
        });

        assertEquals("<style type=\"text/css\"/>", Tr.xsl(new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("doc"));
                ae(L, L, e("style", a("type", "{@styletype}")));
            }
        }.build()).apply(doc));
    }

    @Test
    public void noescape() {
        assertEquals("&", Tr.xsl(new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutput(omitXmlDecl())));
                ae(L, xsTemplate("/"));
                ae(L, L, xsText("&", noescape()));
            }
        }.build()).apply());
    }

    @Test
    public void numberInstruction() {
        String book1 = "Gone with the wind";
        String book2 = "All the rivers run";

        Xml books = new Xml() {
            @Override
            protected void doBuild() {
                ae("books");
                ae(L, e("book", t(book1)));
                ae(L, e("book", t(book2)));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("book"));
                ae(L, L, xsNumber(" 1. "));
                ae(L, L, xsCopyOf("text()"));
            }
        }.build();

        String expected = Str.of(" 1. %s 2. %s").format(book1, book2);

        assertEquals(expected, Tr.xsl(xsl).apply(books));
    }

}
