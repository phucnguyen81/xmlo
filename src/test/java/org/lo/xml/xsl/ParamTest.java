package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

public class ParamTest {

    @Test
    public void withParam() {
        Xml catalog = new Xml() {
            @Override
            protected void doBuild() {
                ae("catalog");
                ae(L, e("item", id("SC-0001")));
                ae(L, L, e("maker", t("Scratchmore")));
                ae(L, L, e("description", t("Wool sweater")));
                ae(L, L, e("size", t("L")));
                ae(L, L, e("price", t("120.00")));
                ae(L, L, e("currency", t("USD")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(noindent(), omitXmlDecl())));
                ae(L, get(this::catalog));
            }

            private void catalog() {
                ae(xsTemplate("catalog"));
                ae(L, xsCopy());
                ae(L, L, xsApply("item"));
                ae(L, L, L, xsWithParam("discount", "0.5"));

                ae(xsTemplate("item"));
                ae(L, xsParam("discount"));
                ae(L, xsCopy());
                ae(L, L, xsAttribute("id", xsValueOf("@id")));
                ae(L, L, xsCopyOf("maker|description|size|price"));
                ae(L, L, e("discount", xsValueOf("$discount")));
                ae(L, L, e("discountPrice"));
                ae(L, L, L, xsValueOf("price - (price * $discount)"));
                ae(L, L, xsCopyOf("currency"));
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<catalog>");
                a(L, "<item id=\"SC-0001\">");
                a(L, L, "<maker>Scratchmore</maker>");
                a(L, L, "<description>Wool sweater</description>");
                a(L, L, "<size>L</size>");
                a(L, L, "<price>120.00</price>");
                a(L, L, "<discount>0.5</discount>");
                a(L, L, "<discountPrice>60</discountPrice>");
                a(L, L, "<currency>USD</currency>");
                a(L, "</item>");
                a("</catalog>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(catalog));
    }

    @Test
    public void param() {
        Xml catalog = new Xml() {
            @Override
            protected void doBuild() {
                ae("catalog");
                ae(L, e("item", id("SC-0001")));
                ae(L, L, e("maker", t("Scratchmore")));
                ae(L, L, e("description", t("Wool sweater")));
                ae(L, L, e("size", t("L")));
                ae(L, L, e("price", t("120.00")));
                ae(L, L, e("currency", t("USD")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(noindent(), omitXmlDecl())));
                ae(L, get(this::catalog));
            }

            private void catalog() {
                ae(xsParam("discount", "0.10"));

                ae(xsTemplate("catalog"));
                ae(L, xsCopy());
                ae(L, L, xsApply("item"));

                ae(xsTemplate("item"));
                ae(L, xsCopy());
                ae(L, L, xsAttribute("id", xsValueOf("@id")));
                ae(L, L, xsCopyOf("maker|description|size|price"));
                ae(L, L, e("discount", xsValueOf("$discount")));
                ae(L, L, e("discountPrice"));
                ae(L, L, L, xsValueOf("price - (price * $discount)"));
                ae(L, L, xsCopyOf("currency"));
            }
        }.build();

        Tr tr = Tr.of(c -> c.xsl(xsl).param("discount", "0.20"));

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<catalog>");
                a(L, "<item id=\"SC-0001\">");
                a(L, L, "<maker>Scratchmore</maker>");
                a(L, L, "<description>Wool sweater</description>");
                a(L, L, "<size>L</size>");
                a(L, L, "<price>120.00</price>");
                a(L, L, "<discount>0.20</discount>");
                a(L, L, "<discountPrice>96</discountPrice>");
                a(L, L, "<currency>USD</currency>");
                a(L, "</item>");
                a("</catalog>");
            }
        }.build();

        assertEquals(expected.get(), tr.apply(catalog));
    }

}