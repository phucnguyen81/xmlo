package org.lo.xml.xsl;

import static org.junit.Assert.*;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Break up stylesheet into small methods, one for each template.
 */
public class XslGetTest {

    @Test
    public void get() {
        Xml provinces = new Xml() {
            @Override
            protected void doBuild() {
                ae("provinces");
                ae(L, e("province", id("AB")));
                ae(L, L, e("name", t("Alberta")));
                ae(L, L, e("abbreviation", t("AB")));
                ae(L, e("province", id("BC")));
                ae(L, L, e("name", t("British Columbia")));
                ae(L, L, e("abbreviation", t("BC")));
            }
        }.build();

        String title = "Provinces of Canada and Abbreviations";

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::provinces));
                ae(L, get(this::province));
                ae(L, get(this::name));
                ae(L, get(this::abbreviation));
            }

            void provinces() {
                ae(xsTemplate("provinces"));
                ae(L, e("title", t(title)), xsLine());
                ae(L, xsApply("province"));
            }

            void province() {
                ae(xsTemplate("province"));
                ae(L, xsApply("name|abbreviation"));
            }

            void name() {
                ae(xsTemplate("name"));
                ae(L, xsApply());
            }

            void abbreviation() {
                ae(xsTemplate("abbreviation"));
                at(L, " (", xsValueOf(), ")");
                ae(L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(title);
                l("Alberta (AB)");
                l("British Columbia (BC)");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(provinces));
    }

}
