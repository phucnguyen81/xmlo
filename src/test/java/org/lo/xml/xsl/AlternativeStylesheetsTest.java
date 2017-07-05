package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test alternative ways of working with stylesheets.
 *
 * @author phuc
 */
public class AlternativeStylesheetsTest {

    @Test
    public void excludeNamespaces() {
        Xml eu = new Xml() {
            @Override
            protected void doBuild() {
                ae("europe");
                ae(L, "scandinavia");
                ae(L, L, e("state", t("Finland")));
                ae(L, L, e("state", t("Sweden")));
                ae(L, L, e("state", t("Iceland")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xmlnsXsl()));
                ae(L, a("xmlns:sc", "http://www.wyeast.net/scand"));
                ae(L, a("xmlns:scand", "http://www.wyeast.net/scandinavia"));
                ae(L, a("xmlns:nr", "http://www.wyeast.net/scandinavia"));
                ae(L, excludeResultPrefixes("scand", "nr"));
                ae(L, xsOutputXml(noindent(), omitXmlDecl()));

                ae(L, xsTemplate("europe"));
                ae(L, L, xsApply("scandinavia"));

                ae(L, xsTemplate("scandinavia"));
                ae(L, L, "sc:scandinavia");
                ae(L, L, L, xsApply("state"));
                ae(L, L, L, L, xsSort());

                ae(L, xsTemplate("state"));
                ae(L, L, "sc:country");
                ae(L, L, L, xsValue());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<sc:scandinavia ");
                a(L, "xmlns:sc=\"http://www.wyeast.net/scand\">");
                a(L, "<sc:country>Finland</sc:country>");
                a(L, "<sc:country>Iceland</sc:country>");
                a(L, "<sc:country>Sweden</sc:country>");
                a("</sc:scandinavia>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(eu));
    }

    @Test
    public void namespaceAlias() {
        Xml eu = new Xml() {
            @Override
            protected void doBuild() {
                ae("europe");
                ae(L, "scandinavia");
                ae(L, L, e("state", t("Finland")));
                ae(L, L, e("state", t("Sweden")));
                ae(L, L, e("state", t("Iceland")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xmlnsXsl()));
                ae(L, a("xmlns", "urn:wyeast-net:scandinavia"));
                ae(L, a("xmlns:sc", "http://www.wyeast.net/scand"));
                ae(L, xsOutputXml(noindent(), omitXmlDecl()));
                ae(L, xsNameSpaceAlias());
                ae(L, L, stylesheetPrefix("sc"), resultPrefix("#default"));

                ae(L, xsTemplate("europe"));
                ae(L, L, xsApply("scandinavia"));

                ae(L, xsTemplate("scandinavia"));
                ae(L, L, "sc:scandinavia");
                ae(L, L, L, xsApply("state"));
                ae(L, L, L, L, xsSort());

                ae(L, xsTemplate("state"));
                ae(L, L, "sc:country");
                ae(L, L, L, xsValue());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<scandinavia xmlns=\"urn:wyeast-net:scandinavia\">");
                a(L, "<country>Finland</country>");
                a(L, "<country>Iceland</country>");
                a(L, "<country>Sweden</country>");
                a("</scandinavia>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(eu));
    }

    @Test
    public void literalResultElement() {
        Xml eu = new Xml() {
            @Override
            protected void doBuild() {
                ae("europe");
                ae(L, "scandinavia");
                ae(L, L, e("state", t("Finland")));
                ae(L, L, e("state", t("Sweden")));
                ae(L, L, e("state", t("Iceland")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(e("scandinavia", xsVersion("1.0"), xmlnsXsl()));
                ae(L, xsForEach("europe/scandinavia/state"));
                ae(L, L, e("country"));
                ae(L, L, L, xsValueOf());
            }
        }.build();

        Tr tr = Tr.of(c -> c.xsl(xsl).omitXmlDecl(true));

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<scandinavia>");
                a(L, "<country>Finland</country>");
                a(L, "<country>Sweden</country>");
                a(L, "<country>Iceland</country>");
                a("</scandinavia>");
            }
        }.build();

        assertEquals(expected.get(), tr.apply(eu.export()));
    }

}