package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for using xsl:key.
 *
 * @author phuc
 */
public class KeyTest {

    @Test
    public void groupMuenchian() {
        Xml cities = new Xml() {
            @Override
            protected void doBuild() {
                ae("uscities");
                ae(L, e("uscity", a("state", "Nevada")));
                ae(L, L, t("Las Vegas"));
                ae(L, e("uscity", a("state", "Arizona")));
                ae(L, L, t("Flagstaff"));
                ae(L, e("uscity", a("state", "Nevada")));
                ae(L, L, t("Silver City"));
                ae(L, e("uscity", a("state", "California")));
                ae(L, L, t("Los Angeles"));
                ae(L, e("uscity", a("state", "Arizona")));
                ae(L, L, t("Phoenix"));
                ae(L, e("uscity", a("state", "California")));
                ae(L, L, t("San Francisco"));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                // select distince states by Muenchian method
                String eachState = "uscity[generate-id(.)=generate-id(key('State', @state))]/@state";

                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("State", "uscity", "@state"));
                ae(L, xsTemplate("uscities"));
                ae(L, L, xsForEach(eachState));
                ae(L, L, L, xsSort());
                ae(L, L, L, xsValue(), xsLine());
                ae(L, L, L, xsForEach("key('State', .)"));
                ae(L, L, L, L, xsSort());
                ae(L, L, L, L, xsText("  "), xsValue(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Arizona");
                l("  Flagstaff");
                l("  Phoenix");
                l("California");
                l("  Los Angeles");
                l("  San Francisco");
                l("Nevada");
                l("  Las Vegas");
                l("  Silver City");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(cities));
    }

    @Test
    public void paramWithKey() {
        Xml un = new Xml() {
            @Override
            protected void doBuild() {
                ae("un");
                ae(L, e("state", a("cc", "af")));
                ae(L, L, e("name", t("Afghanistan")));
                ae(L, L, e("admitted", t("19 Nov. 1946")));
                ae(L, e("state", a("cc", "au")));
                ae(L, L, e("name", t("Australia")));
                ae(L, L, e("admitted", t("1 Nov. 1945")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("UN", "state", "@cc"));
                ae(L, xsParam("kp", t("af")));
                ae(L, xsTemplate("un"));
                ae(L, L, xsValue("key('UN',$kp)/name"));
            }
        }.build();

        Tr tr = Tr.of(c -> c.xsl(xsl).param("kp", "au"));

        assertEquals("Australia", tr.apply(un));
    }

    @Test
    public void crossReferences() {
        Xml us = new Xml() {
            @Override
            protected void doBuild() {
                ae("usstates");
                ae(L, e("western"));
                ae(L, L, e("usstate", t("Arizona")));
                ae(L, L, e("usstate", t("California")));
                ae(L, "capitals");
                ae(L, L, e("capital", a("usstate", "Arizona")));
                ae(L, L, L, t("Phoenix"));
                ae(L, L, e("capital", a("usstate", "California")));
                ae(L, L, L, t("Sacramento"));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                String state = "usstates/western/usstate";
                String capital = "usstates/capitals/capital";

                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("State", state, "."));
                ae(L, xsKey("Capital", capital, "@usstate"));

                ae(L, xsTemplate("usstates"));
                ae(L, L, xsValue("key('State','Arizona')"));
                ae(L, L, xsText(", "));
                ae(L, L, xsValue("key('Capital','Arizona')"));
            }
        }.build();

        assertEquals("Arizona, Phoenix", Tr.xsl(xsl).apply(us));
    }

    @Test
    public void multipleKeys() {
        Xml un = new Xml() {
            @Override
            protected void doBuild() {
                ae("un");
                ae(L, e("state", a("cc", "af")));
                ae(L, L, e("name", t("Afghanistan")));
                ae(L, L, e("admitted", t("19 Nov. 1946")));
                ae(L, e("state", a("cc", "au")));
                ae(L, L, e("name", t("Australia")));
                ae(L, L, e("admitted", t("1 Nov. 1945")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("UN", "state", "@cc"));
                ae(L, xsKey("State", "state", "name"));
                ae(L, xsTemplate("un"));
                ae(L, L, xsValue("key('UN','au')/name"));
                ae(L, L, xsText(" "));
                ae(L, L, xsValue("key('State','Australia')/admitted"));
            }
        }.build();

        assertEquals("Australia 1 Nov. 1945", Tr.xsl(xsl).apply(un));
    }

    /**
     * The key() function returns the first matched node, not all matched nodes.
     */
    @Test
    public void multipleValues() {
        Xml un = new Xml() {
            @Override
            protected void doBuild() {
                ae("items");
                ae(L, e("item", a("name", "cup")));
                ae(L, L, e("id", t("1")));
                ae(L, e("item", a("name", "cup")));
                ae(L, L, e("id", t("2")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("Name", "item", "@name"));
                ae(L, xsTemplate("items"));
                ae(L, L, xsValue("key('Name','cup')"));
            }
        }.build();

        assertEquals("1", Tr.xsl(xsl).apply(un));
    }

    /** Defines a key then query the node of a given key */
    @Test
    public void simple() {
        Xml un = new Xml() {
            @Override
            protected void doBuild() {
                ae("un");
                ae(L, e("state", a("cc", "af")));
                ae(L, L, e("name", t("Afghanistan")));
                ae(L, L, e("admitted", t("19 Nov. 1946")));
                ae(L, e("state", a("cc", "au")));
                ae(L, L, e("name", t("Australia")));
                ae(L, L, e("admitted", t("1 Nov. 1945")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsKey("UN", "state", "@cc"));
                ae(L, xsTemplate("un"));
                ae(L, L, xsValue("key('UN','au')/name"));
            }
        }.build();

        assertEquals("Australia", Tr.xsl(xsl).apply(un));
    }

}