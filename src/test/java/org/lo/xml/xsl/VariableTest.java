package org.lo.xml.xsl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

public class VariableTest {

    @Test
    public void variable() {
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
                ae(xsVariable("discount", "0.10"));

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

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                a("<catalog>");
                a(L, "<item id=\"SC-0001\">");
                a(L, L, "<maker>Scratchmore</maker>");
                a(L, L, "<description>Wool sweater</description>");
                a(L, L, "<size>L</size>");
                a(L, L, "<price>120.00</price>");
                a(L, L, "<discount>0.1</discount>");
                a(L, L, "<discountPrice>108</discountPrice>");
                a(L, L, "<currency>USD</currency>");
                a(L, "</item>");
                a("</catalog>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(catalog));
    }

    /**
     * Tree fragment can be stored in variable and refered to with copy-of.
     */
    @Test
    public void variableFragment() {
        Document catalog = new Xml() {
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
        }.build().export();

        Xsl xsl = new Xsl() {

            String discountPrice = "format-number(/catalog/item/price * 0.60, '###.00')";

            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputXml(noindent(), omitXmlDecl())));
                ae(L, xsVariable("discount"));
                ae(L, L, e("discount", t("0.40")));
                ae(L, L, e("discountPrice"));
                ae(L, L, L, xsValueOf(discountPrice));
                ae(L, get(this::catalog));
            }

            private void catalog() {
                ae(xsTemplate("catalog"));
                ae(L, xsCopy());
                ae(L, L, xsApply("item"));

                ae(xsTemplate("item"));
                ae(L, xsCopy());
                ae(L, L, xsCopyOf("@id"));
                ae(L, L, xsCopyOf("maker|description|size|price"));
                ae(L, L, xsCopyOf("$discount"));
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
                a(L, L, "<discount>0.40</discount>");
                a(L, L, "<discountPrice>72.00</discountPrice>");
                a(L, L, "<currency>USD</currency>");
                a(L, "</item>");
                a("</catalog>");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(catalog));
    }

}
