package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for xsl:sort.
 *
 * @author phuc
 */
public class SortTest {

    @Test
    public void multipleSorts() {
        Xml shopping = new Xml() {
            @Override
            protected void doBuild() {
                ae("list");
                ae(L, "freezer");
                ae(L, L, e("item", t("peas")));
                ae(L, L, e("item", t("green beans")));
                ae(L, L, e("item", t("pot pie")));
                ae(L, L, e("item", t("ice cream")));
                ae(L, "bakery");
                ae(L, L, e("item", t("rolls")));
                ae(L, L, e("item", t("jelly doughnuts")));
                ae(L, L, e("item", t("bread")));
                ae(L, "produce");
                ae(L, L, e("item", t("bananas")));
                ae(L, L, e("item", t("kumquats")));
                ae(L, L, e("item", t("apples")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::list));
                ae(L, get(this::star));
                ae(L, get(this::item));
            }

            /* sort lists by tag-name */
            void list() {
                ae(xsTemplate("list"));
                ae(L, xsApply("*"));
                ae(L, L, xsSort("name()"));
            }

            /* sort items by content */
            void star() {
                ae(xsTemplate("*"));
                ae(L, xsText("Section: "));
                ae(L, xsValueOf("name()"));
                ae(L, xsLine());
                ae(L, xsApply("item"));
                ae(L, L, xsSort());
            }

            void item() {
                ae(xsTemplate("item"));
                ae(L, xsText(" * "));
                ae(L, xsApply());
                ae(L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Section: bakery");
                l(" * bread");
                l(" * jelly doughnuts");
                l(" * rolls");
                l("Section: freezer");
                l(" * green beans");
                l(" * ice cream");
                l(" * peas");
                l(" * pot pie");
                l("Section: produce");
                l(" * apples");
                l(" * bananas");
                l(" * kumquats");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(shopping));
    }

    @Test
    public void orderDescending() {
        Xml europe = new Xml() {
            @Override
            protected void doBuild() {
                ae("europe");
                ae(L, e("state", t("Belgium")));
                ae(L, e("state", t("Germany")));
                ae(L, e("state", t("Finland")));
                ae(L, e("state", t("Greece")));
                ae(L, e("state", t("Ireland")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputHtml(noindent())));
                ae(L, get(this::europe));
                ae(L, get(this::state));
            }

            void europe() {
                ae(xsTemplate("europe"));
                ae(L, "html");
                ae(L, L, get(this::head));
                ae(L, L, get(this::body));
            }

            void head() {
                ae(e("head", e("title", t("European States"))));
                ae(L, e("style", a("type", "text/css")));
                ae(L, L, t("body {font-family: sans-serif}"));
            }

            void body() {
                ae("body");
                ae(L, e("h3", t("Some European States")));
                ae(L, e("p", e("b", t("Count:")), xsText(" ")));
                ae(L, L, xsValueOf("count(state)"));
                ae(L, get(this::ul));
            }

            void ul() {
                ae("ul");
                ae(L, xsApply("state"));
                ae(L, L, xsSort(orderDescending()));
            }

            void state() {
                ae(xsTemplate("state"));
                ae(L, "li");
                ae(L, L, xsApply());
            }
        }.build();

        Str expected = new Str() {
            String meta = "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";

            @Override
            protected void doBuild() {
                a("<html>");
                a(L, "<head>");
                a(L, L, meta);
                a(L, L, "<title>European States</title>");
                a(L, L, "<style type=\"text/css\">");
                a(L, L, L, "body {font-family: sans-serif}");
                a(L, L, "</style>");
                a(L, "</head>");
                a(L, "<body>");
                a(L, L, "<h3>Some European States</h3>");
                a(L, L, "<p><b>Count:</b> 5</p>");
                a(L, L, "<ul>");
                a(L, L, L, "<li>Ireland</li>");
                a(L, L, L, "<li>Greece</li>");
                a(L, L, L, "<li>Germany</li>");
                a(L, L, L, "<li>Finland</li>");
                a(L, L, L, "<li>Belgium</li>");
                a(L, L, "</ul>");
                a(L, "</body>");
                a("</html>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(europe));
    }

    @Test
    public void basic() {
        Xml europe = new Xml() {
            @Override
            protected void doBuild() {
                ae("europe");
                ae(L, e("state", t("Belgium")));
                ae(L, e("state", t("Germany")));
                ae(L, e("state", t("Finland")));
                ae(L, e("state", t("Greece")));
                ae(L, e("state", t("Ireland")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::europe));
                ae(L, get(this::state));
            }

            private void europe() {
                ae(xsTemplate("europe"));
                ae(L, xsLine("Some European States"));
                ae(L, xsText("Count: "), xsValueOf("count(state)"));
                ae(L, xsLine());
                ae(L, xsLine());
                ae(L, xsApply("state"));
                ae(L, L, xsSort());
            }

            private void state() {
                ae(xsTemplate("state"));
                ae(L, xsApply());
                ae(L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("Some European States");
                l("Count: 5");
                l();
                l("Belgium");
                l("Finland");
                l("Germany");
                l("Greece");
                l("Ireland");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(europe));
    }

}