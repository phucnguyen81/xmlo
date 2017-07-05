package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Document;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for xsl:template.
 *
 * @author phuc
 */
public class TemplateTest {

    @Test
    public void builtInTemplates() {
        Xml state = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("mammals", a("locale", "North America")));
                ae(L, e("mammal", t("Bobcat")));
                ae(L, e("mammal", t("Cougar")));
                ae(L, e("mammal", t("Pronghorn")));
            }
        }.build();

        Xsl xsl = Xsl.create(x -> {
            x.ae(x.xslStyleSheetV1(x.xsOutputText()));
        });

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("Bobcat");
                a("Cougar");
                a("Pronghorn");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(state));
    }

    @Test
    public void applyTemplateWithMode() {
        Document state = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("state", name("Rhode Island")));
                ae(L, e("county", t("Bristol")));
                ae(L, e("county", t("Kent")));
                ae(L, e("county", t("Newport")));
            }
        }.build().export();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::root));
                ae(L, get(this::stateB));
                ae(L, get(this::stateK));
                ae(L, get(this::county));
            }

            void root() {
                ae(xsTemplate("/"));
                ae(L, xsText("State: "), xsValue("state/@name"));
                ae(L, xsLine());
                ae(L, xsApply("state", mode("B")));
                ae(L, xsApply("state", mode("K")));
            }

            void stateB() {
                ae(xsTemplate("state", mode("B")));
                ae(L, xsApply("county[starts-with(.,'B')]"));
            }

            void stateK() {
                ae(xsTemplate("state", mode("K")));
                ae(L, xsApply("county[starts-with(.,'K')]"));
            }

            void county() {
                ae(xsTemplate("county"));
                ae(L, xsText("  "), xsApply(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("State: Rhode Island");
                l("  Bristol");
                l("  Kent");
            }
        }.build();

        Tr tr = Tr.of(c -> c.xsl(xsl));

        assertEquals(expected.get(), tr.apply(state));
    }

    @Test
    public void callTemplateWithParam() {
        Xml state = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("state", name("Rhode Island")));
                ae(L, e("county", t("Bristol")));
                ae(L, e("county", t("Kent")));
                ae(L, e("county", t("Newport")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::state));
                ae(L, get(this::stateStarts));
                ae(L, get(this::county));
            }

            void state() {
                ae(xsTemplate("state"));
                ae(L, xsText("State: "), xsValue("@name"), xsLine());
                ae(L, xsCallTemplate("state-starts"));
                ae(L, L, xsWithParam("starts", "'B'"));
                ae(L, xsCallTemplate("state-starts"));
                ae(L, L, xsWithParam("starts", "'K'"));
            }

            void stateStarts() {
                ae(xsTemplate(name("state-starts")));
                ae(L, xsParam("starts"));
                ae(L, xsApply("county[starts-with(.,$starts)]"));
            }

            void county() {
                ae(xsTemplate("county"));
                ae(L, xsText("  "), xsApply(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("State: Rhode Island");
                l("  Bristol");
                l("  Kent");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(state));
    }

    @Test
    public void callTemplate() {
        Xml state = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("state", name("Rhode Island")));
                ae(L, e("county", t("Bristol")));
                ae(L, e("county", t("Kent")));
                ae(L, e("county", t("Newport")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::state));
                ae(L, get(this::stateB));
                ae(L, get(this::stateK));
                ae(L, get(this::county));
            }

            void state() {
                ae(xsTemplate("state"));
                ae(L, xsText("State: "), xsValue("@name"), xsLine());
                ae(L, xsCallTemplate("stateB"));
                ae(L, xsCallTemplate("stateK"));
            }

            void stateB() {
                ae(xsTemplate(name("stateB")));
                ae(L, xsApply("county[starts-with(.,'B')]"));
            }

            void stateK() {
                ae(xsTemplate(name("stateK")));
                ae(L, xsApply("county[starts-with(.,'K')]"));
            }

            void county() {
                ae(xsTemplate("county"));
                ae(L, xsText("  "), xsApply(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("State: Rhode Island");
                l("  Bristol");
                l("  Kent");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(state));
    }

    @Test
    public void priority() {
        Xml state = new Xml() {
            @Override
            protected void doBuild() {
                ae(e("state", name("Rhode Island")));
                ae(L, e("county", t("Bristol")));
                ae(L, e("county", t("Kent")));
                ae(L, e("county", t("Newport")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::state));
                ae(L, get(this::stateK));
                ae(L, get(this::county));
            }

            void state() {
                ae(xsTemplate("state", priority(2)));
                ae(L, xsApply("county"));
            }

            void stateK() {
                ae(xsTemplate("state", priority(1)));
                ae(L, xsApply("county[starts-with(.,'K')]"));
            }

            void county() {
                ae(xsTemplate("county"));
                ae(L, xsApply(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Bristol");
                l("Kent");
                l("Newport");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(state));
    }

}