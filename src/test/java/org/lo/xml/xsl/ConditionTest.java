package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for using conditions in xslt.
 *
 * @author phuc
 */
public class ConditionTest {

    @Test
    public void chooseWhen() {
        Xml africa = new Xml() {
            @Override
            protected void doBuild() {
                ae("africa");
                ae(L, "nation");
                ae(L, L, e("name", t("Algeria")));
                ae(L, L, e("capital", t("Algiers")));
                ae(L, L, e("population", t(32277942)));
                ae(L, L, e("cc", t("dz")));
                ae(L, "nation");
                ae(L, L, e("name", t("Benin")));
                ae(L, L, e("capital", t("Porto-Novo")));
                ae(L, L, e("population", t(6787625)));
                ae(L, L, e("cc", t("bj")));
                ae(L, "nation");
                ae(L, L, e("name", t("Bostwana")));
                ae(L, L, e("capital", t("Gaborone")));
                ae(L, L, e("population", t(1591232)));
                ae(L, L, e("cc", t("bj")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("nation"));
                ae(L, L, xsChoose());
                ae(L, L, L, xsWhen("population > 10000000"));
                ae(L, L, L, L, xsText(" * "), xsValue("name"));
                ae(L, L, L, L, xsText(" (over 10M)"));
                ae(L, L, L, L, xsLine());
                ae(L, L, L, xsWhen("population > 5000000"));
                ae(L, L, L, L, xsText(" * "), xsValue("name"));
                ae(L, L, L, L, xsText(" (over 5M)"));
                ae(L, L, L, L, xsLine());
                ae(L, L, L, xsOtherwise());
                ae(L, L, L, L, xsText(" * "), xsValue("name"));
                ae(L, L, L, L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" * Algeria (over 10M)");
                l(" * Benin (over 5M)");
                l(" * Bostwana");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(africa));
    }

    @Test
    public void predicate() {
        Xml africa = new Xml() {
            @Override
            protected void doBuild() {
                ae("africa");
                ae(L, "nation");
                ae(L, L, e("name", t("Algeria")));
                ae(L, L, e("capital", t("Algiers")));
                ae(L, L, e("population", t(32277942)));
                ae(L, L, e("cc", t("dz")));
                ae(L, "nation");
                ae(L, L, e("name", t("Benin")));
                ae(L, L, e("capital", t("Porto-Novo")));
                ae(L, L, e("population", t(6787625)));
                ae(L, L, e("cc", t("bj")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("nation"));
                ae(L, L, xsText(" * "), xsValue("name"));
                ae(L, L, xsLine());
                ae(L, xsTemplate("nation[population > 10000000]"));
                ae(L, L, xsText(" * "), xsValue("name"));
                ae(L, L, xsText(" (over 10M)"));
                ae(L, L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" * Algeria (over 10M)");
                l(" * Benin");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(africa));
    }

    @Test
    public void ifCondition() {
        Xml africa = new Xml() {
            @Override
            protected void doBuild() {
                ae("africa");
                ae(L, "nation");
                ae(L, L, e("name", t("Algeria")));
                ae(L, L, e("capital", t("Algiers")));
                ae(L, L, e("population", t(32277942)));
                ae(L, L, e("cc", t("dz")));
                ae(L, "nation");
                ae(L, L, e("name", t("Benin")));
                ae(L, L, e("capital", t("Porto-Novo")));
                ae(L, L, e("population", t(6787625)));
                ae(L, L, e("cc", t("bj")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("nation"));
                ae(L, L, xsText(" * "), xsValue("name"));
                ae(L, L, xsIf("population > 10000000"));
                ae(L, L, L, xsText(" (over 10M)"));
                ae(L, L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" * Algeria (over 10M)");
                l(" * Benin");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(africa));
    }

}