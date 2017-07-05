package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for xsl include/import/document.
 *
 * @author phuc
 */
public class MultipleStylesheetsTest {

    @Test
    public void documentFunction() {
        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(omitXmlDecl())));
                ae(L, xsTemplate("/"));
                ae(L, L, xsCopyOf("document('Book')"));
            }
        }.build();

        Xml book = new Xml() {
            @Override
            protected void doBuild() {
                ae("volume");
                ae(L, "book");
            }
        }.build();

        URIResolver resolver = (href, base) -> {
            if ("Book".equals(href)) {
                return new StreamSource(new StringReader(book.toXml()));
            } else {
                return null;
            }
        };

        Tr tr = Tr.of(c -> c.xsl(xsl).resolver(resolver));

        assertEquals("<volume><book/></volume>", tr.apply());
    }

    @Test
    public void applyImports() {
        Xml population = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("PopulationChange", a("segment", "Top 3")));
                ae(L, "State");
                ae(L, L, e("Name", t("California")));
                ae(L, L, e("Population", t(35116033)));
                ae(L, L, e("Rank", t(1)));
                ae(L, L, e("Increase", t(515570)));
                ae(L, L, e("PercentChange", t(1.5)));
                ae(L, "State");
                ae(L, L, e("Name", t("Texas")));
                ae(L, L, e("Population", t(21779893)));
                ae(L, L, e("Rank", t(2)));
                ae(L, L, e("Increase", t(408910)));
                ae(L, L, e("PercentChange", t(1.9)));
                ae(L, "State");
                ae(L, L, e("Name", t("New York")));
                ae(L, L, e("Population", t(19157532)));
                ae(L, L, e("Rank", t(3)));
                ae(L, L, e("Increase", t(73182)));
                ae(L, L, e("PercentChange", t(0.4)));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsImport("Rank"));

                ae(L, xsTemplate("PopulationChange"));
                ae(L, L, xsText("Population Change"), xsLine());
                ae(L, L, xsApply("State"));

                ae(L, xsTemplate("State"));
                ae(L, L, xsText("State: "), xsValue("Name"));
                ae(L, L, xsText(", "));
                ae(L, L, xsApplyImports());
            }
        }.build();

        Xsl state = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));

                ae(L, xsTemplate("State"));
                ae(L, L, xsText("Rank: "), xsValue("Rank"));
                ae(L, L, xsLine());
            }
        }.build();

        URIResolver resolver = (href, base) -> {
            if ("Rank".equals(href)) {
                return new StreamSource(new StringReader(state.toXml()));
            } else {
                return null;
            }
        };

        Tr tr = Tr.of(c -> c.xsl(xsl).resolver(resolver));

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Population Change");
                l("State: California, Rank: 1");
                l("State: Texas, Rank: 2");
                l("State: New York, Rank: 3");
            }
        }.build();

        assertEquals(expected.get(), tr.apply(population));
    }

    @Test
    public void importXsl() {
        Xml population = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("PopulationChange", a("segment", "Top 3")));
                ae(L, "State");
                ae(L, L, e("Name", t("California")));
                ae(L, L, e("Population", t(35116033)));
                ae(L, L, e("Rank", t(1)));
                ae(L, L, e("Increase", t(515570)));
                ae(L, L, e("PercentChange", t(1.5)));
                ae(L, "State");
                ae(L, L, e("Name", t("Texas")));
                ae(L, L, e("Population", t(21779893)));
                ae(L, L, e("Rank", t(2)));
                ae(L, L, e("Increase", t(408910)));
                ae(L, L, e("PercentChange", t(1.9)));
                ae(L, "State");
                ae(L, L, e("Name", t("New York")));
                ae(L, L, e("Population", t(19157532)));
                ae(L, L, e("Rank", t(3)));
                ae(L, L, e("Increase", t(73182)));
                ae(L, L, e("PercentChange", t(0.4)));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsImport("State"));

                ae(L, xsTemplate("PopulationChange"));
                ae(L, L, xsText("Population Change"), xsLine());
                ae(L, L, xsApply("State"));

                ae(L, xsTemplate("State"));
                ae(L, L, xsText("State: "), xsValue("Name"));
                ae(L, L, xsText(", "));
                ae(L, L, xsText("Rank: "), xsValue("Rank"));
                ae(L, L, xsCall("newline"));
            }
        }.build();

        Xsl include = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));

                // this has lower priority than the including template
                ae(L, xsTemplate("State"));
                ae(L, L, xsText("Rank: "), xsValue("Rank"));
                ae(L, L, xsText(", "));
                ae(L, L, xsText("State: "), xsValue("Name"));
                ae(L, L, xsCall("newline"));

                ae(L, xsTemplate(name("newline")));
                ae(L, L, xsLine());
            }
        }.build();

        URIResolver resolver = (href, base) -> {
            if ("State".equals(href)) {
                return new StreamSource(new StringReader(include.toXml()));
            } else {
                return null;
            }
        };

        Tr tr = Tr.of(c -> c.xsl(xsl).resolver(resolver));

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Population Change");
                l("State: California, Rank: 1");
                l("State: Texas, Rank: 2");
                l("State: New York, Rank: 3");
            }
        }.build();

        assertEquals(expected.get(), tr.apply(population));
    }

    @Test
    public void include() {
        Xml population = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("PopulationChange", a("segment", "Top 3")));
                ae(L, "State");
                ae(L, L, e("Name", t("California")));
                ae(L, L, e("Population", t(35116033)));
                ae(L, L, e("Rank", t(1)));
                ae(L, L, e("Increase", t(515570)));
                ae(L, L, e("PercentChange", t(1.5)));
                ae(L, "State");
                ae(L, L, e("Name", t("Texas")));
                ae(L, L, e("Population", t(21779893)));
                ae(L, L, e("Rank", t(2)));
                ae(L, L, e("Increase", t(408910)));
                ae(L, L, e("PercentChange", t(1.9)));
                ae(L, "State");
                ae(L, L, e("Name", t("New York")));
                ae(L, L, e("Population", t(19157532)));
                ae(L, L, e("Rank", t(3)));
                ae(L, L, e("Increase", t(73182)));
                ae(L, L, e("PercentChange", t(0.4)));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsInclude("State"));
                ae(L, xsTemplate("PopulationChange"));
                ae(L, L, xsText("Population Change"), xsLine());
                ae(L, L, xsApply("State"));
            }
        }.build();

        Xsl include = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("State"));
                ae(L, L, xsText("State: "), xsValue("Name"), xsText(", "));
                ae(L, L, xsText("Rank: "), xsValue("Rank"), xsLine());
            }
        }.build();

        URIResolver resolver = (href, base) -> {
            if ("State".equals(href)) {
                return new StreamSource(new StringReader(include.toXml()));
            } else {
                return null;
            }
        };

        Tr tr = Tr.of(c -> c.xsl(xsl).resolver(resolver));

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Population Change");
                l("State: California, Rank: 1");
                l("State: Texas, Rank: 2");
                l("State: New York, Rank: 3");
            }
        }.build();

        assertEquals(expected.get(), tr.apply(population));
    }

}