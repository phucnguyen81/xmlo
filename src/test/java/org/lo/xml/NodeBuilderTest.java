package org.lo.xml;

import static org.junit.Assert.*;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;

public class NodeBuilderTest {

    /**
     * Making all seven types of nodes of XPath 1.0: Root, Element, Attribute,
     * Text, Namespace, Comment, and Processing instruction.
     */
    @Test
    public void nodes() {
        // processing instruction
        String piData = "href=\"tree-view.xsl\" type=\"text/xsl\"";

        assertEquals(new Str() {
            @Override
            protected void doBuild() {
                a("<?xml-stylesheet ", piData, "?>");
                a("<!-- Last invoice of day's batch -->");
                a("<amount xmlns=\"urn:wyeast-net:invoice\"");
                a(" vendor=\"314\" xml:lang=\"en\">7598.00</amount>");
            }
        }.make(), F.toString(new Xml() {
            @Override
            protected void doBuild() {
                ae(pi("xml-stylesheet", piData));
                ac(" Last invoice of day's batch ");
                ae(e("amount", a("xmlns", "urn:wyeast-net:invoice")));
                ae(L, a("vendor", 314), a("xml:lang", "en"));
                ae(L, t("7598.00"));
            }
        }.build().export()));
    }

    @Test
    public void export() {
        assertEquals(new Str() {
            @Override
            protected void doBuild() {
                a("<!--XML member-->");
                a("<name>");
                a(L, "<last>Angerstein</last>");
                a(L, "<first>Paula</first>");
                a("</name>");
            }
        }.make(), F.toString(new Xml() {
            @Override
            protected void doBuild() {
                ae(c("XML member"));
                ae("name");
                ae(L, e("last", t("Angerstein")));
                ae(L, e("first", t("Paula")));
            }
        }.build().export()));
    }

    @Test
    public void cdata() {
        String data = "Author & British prime minister";

        assertEquals(new Str() {
            @Override
            protected void doBuild() {
                a("<name>");
                a(L, "<family>Churchill</family>");
                a(L, "<given>Winston</given>");
                a(L, "<notes><![CDATA[", data, "]]></notes>");
                a("</name>");
            }
        }.make(), new Xml() {
            @Override
            protected void doBuild() {
                ae("name");
                ae(L, e("family", t("Churchill")));
                ae(L, e("given", t("Winston")));
                ae(L, e("notes", cdata(data)));
            }
        }.build().toXml());
    }

    @Test
    public void company() {
        assertEquals(new Str() {
            @Override
            protected void doBuild() {
                a("<company>");
                a(L, "<staff id=\"1\">");
                a(L, L, "<firstname>yong</firstname>");
                a(L, L, "<lastname>mook kim</lastname>");
                a(L, L, "<nickname>mkyong</nickname>");
                a(L, L, "<salary>100000</salary>");
                a(L, "</staff>");
                a("</company>");
            }
        }.make(), new Xml() {
            @Override
            protected void doBuild() {
                ae("company");
                ae(L, e("staff", a("id", 1)));
                ae(L, L, e("firstname", t("yong")));
                ae(L, L, e("lastname", t("mook kim")));
                ae(L, L, e("nickname", t("mkyong")));
                ae(L, L, e("salary", t(100000)));
            }
        }.build().toXml());
    }

    @Test
    public void orders() {
        assertEquals(new Str() {
            @Override
            protected void doBuild() {
                a("<orders>");
                a(L, "<!--good choice-->");
                a(L, "<order id=\"553\">");
                a(L, L, "<amount>$45.00</amount>");
                a(L, "</order>");
                a("</orders>");
            }
        }.make(), new Xml() {
            @Override
            protected void doBuild() {
                ae("orders");
                ae(L, c("good choice"));
                ae(L, e("order", id(553)));
                ae(L, L, e("amount", t("$45.00")));
            }
        }.build().toXml());
    }

    @Test
    public void addArrayOfChildren() {
        assertEquals("<name><first/><last/></name>", new Xml() {
            @Override
            protected void doBuild() {
                ae("name");
                ae(L, new Object[] { e("first"), e("last") });
            }
        }.build().toXml());
    }

}
