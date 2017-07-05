package org.lo.xml.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import org.lo.xml.Xml;
import org.lo.xml.Str;
import org.lo.xml.Tr;
import org.lo.xml.Xsl;

/**
 * Test case for numbering lists with xslt.
 *
 * @author phuc
 */
public class NumberingTest {

    @Test
    @Ignore("from does not seem to work with default jdk transformer")
    public void countFrom() {
        Xml currency = new Xml() {
            @Override
            protected void doBuild() {
                ae("outline");
                ae(L, e("section", a("title", "US coin denominations")));
                ae(L, L, e("item", t("cent")));
                ae(L, L, e("item", t("nickel")));
                ae(L, L, e("item", t("dime")));
                ae(L, e("section", a("title", "Persons on US coins")));
                ae(L, L, e("item", t("Abraham Lincoln (cent)")));
                ae(L, L, e("item", t("Thomas Jefferson (nickel)")));
                ae(L, L, e("item", t("Franklin Roosevelt (dime)")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::outline));
            }

            void outline() {
                ae(xsTemplate("outline"));
                ae(L, xsForEach("section|//item"));
                ae(L, L, xsNumber(levelMultiple(), from("section")));
                ae(L, L, L, count("section | item"), format("1. "));
                ae(L, L, xsValue("@title|text()"));
                ae(L, L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("US coin denominations");
                l("1. cent");
                l("2. nickel");
                l("3. dime");
                l("Persons on US coins");
                l("1. Abraham Lincoln (cent)");
                l("2. Thomas Jefferson (nickel)");
                l("3. Franklin Roosevelt (dime)");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(currency));
    }

    @Test
    public void multipleLevelsManually() {
        Xml currency = new Xml() {
            @Override
            protected void doBuild() {
                ae("outline");
                ae(L, e("section", a("title", "US coin denominations")));
                ae(L, L, e("item", t("cent")));
                ae(L, L, e("item", t("nickel")));
                ae(L, L, e("item", t("dime")));
                ae(L, e("section", a("title", "Persons on US coins")));
                ae(L, L, e("item", t("Abraham Lincoln (cent)")));
                ae(L, L, e("item", t("Thomas Jefferson (nickel)")));
                ae(L, L, e("item", t("Franklin Roosevelt (dime)")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::outline));
                ae(L, get(this::section));
                ae(L, get(this::item));
            }

            void outline() {
                ae(xsTemplate("outline"));
                ae(L, xsApply("section"));
            }

            void section() {
                ae(xsTemplate("section"));
                ae(L, xsNumber("a. "));
                ae(L, xsValue("@title"), xsLine());
                ae(L, xsApply("item"));
            }

            void item() {
                ae(xsTemplate("item"));
                ae(L, xsNumber("   1. "));
                ae(L, xsValue("text()"), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("a. US coin denominations");
                l("   1. cent");
                l("   2. nickel");
                l("   3. dime");
                l("b. Persons on US coins");
                l("   1. Abraham Lincoln (cent)");
                l("   2. Thomas Jefferson (nickel)");
                l("   3. Franklin Roosevelt (dime)");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(currency));
    }

    @Test
    public void multipleLevels() {
        Xml currency = new Xml() {
            @Override
            protected void doBuild() {
                ae("outline");
                ae(L, e("section", a("title", "US coin denominations")));
                ae(L, L, e("item", t("cent")));
                ae(L, L, e("item", t("nickel")));
                ae(L, L, e("item", t("dime")));
                ae(L, e("section", a("title", "Persons on US coins")));
                ae(L, L, e("item", t("Abraham Lincoln (cent)")));
                ae(L, L, e("item", t("Thomas Jefferson (nickel)")));
                ae(L, L, e("item", t("Franklin Roosevelt (dime)")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::outline));
            }

            void outline() {
                ae(xsTemplate("outline"));
                ae(L, xsForEach("section|//item"));
                ae(L, L, xsNumber(levelMultiple()));
                ae(L, L, L, count("section|item"), format("a. 1. "));
                ae(L, L, xsValue("@title|text()"));
                ae(L, L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("a. US coin denominations");
                l("a. 1. cent");
                l("a. 2. nickel");
                l("a. 3. dime");
                l("b. Persons on US coins");
                l("b. 1. Abraham Lincoln (cent)");
                l("b. 2. Thomas Jefferson (nickel)");
                l("b. 3. Franklin Roosevelt (dime)");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(currency));
    }

    @Test
    public void decimalFormat() {
        Xml numbers = new Xml() {
            @Override
            protected void doBuild() {
                ae("format");
                ae(L, e("number", t(1_000_000)));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::decimalFormat));
                ae(L, get(this::number));
            }

            void decimalFormat() {
                ae(xsDecimalFormat(name("de")));
                ae(L, groupSep("."), decimalSep(","));
                ae(xsDecimalFormat(name("fr")));
                ae(L, groupSep(" "), decimalSep(","));
                ae(xsDecimalFormat(name("us")));
                ae(L, groupSep(","), decimalSep("."));
                ae(xsDecimalFormat(name("ru")));
                ae(L, groupSep(" "), decimalSep(","));
            }

            void number() {
                ae(xsTemplate("number"));
                ae(L, xsValue("format-number(.,'.###,00\u20AC','de')"));
                ae(L, xsLine());
                ae(L, xsValue("format-number(.,' ###,00\u20AC','fr')"));
                ae(L, xsLine());
                ae(L, xsValue("format-number(.,',###.00\u20AC','us')"));
                ae(L, xsLine());
                ae(L, xsValue("format-number(.,' ###,00p.','ru')"));
                ae(L, xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("1.000.000,00\u20AC");
                l("1 000 000,00\u20AC");
                l("1,000,000.00\u20AC");
                l("1 000 000,00p.");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(numbers));
    }

    @Test
    public void formatNumber() {
        Xml thanks = Xml.create("thanks", "Thanks a ");

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, xsTemplate("thanks"));
                ae(L, L, xsValue());
                ae(L, L, xsNumber(value(1_000_000)));
                ae(L, L, L, groupSize(3), groupSep(","));
                ae(L, L, xsText("!"));
            }
        }.build();

        assertEquals("Thanks a 1,000,000!", Tr.xsl(xsl).apply(thanks));
    }

    @Test
    public void romanNumerals() {
        Xml provinces = new Xml() {
            @Override
            protected void doBuild() {
                ae("provinces");
                ae(L, e("name", t("Alberta")));
                ae(L, e("name", t("Manitoba")));
                ae(L, e("name", t("Ontario")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::provinces));
                ae(L, get(this::name));
            }

            void provinces() {
                ae(xsTemplate("provinces"));
                ae(L, xsApply("name"));
            }

            void name() {
                ae(xsTemplate("name"));
                ae(L, xsNumber(" i. "), xsValue(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" i. Alberta");
                l(" ii. Manitoba");
                l(" iii. Ontario");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(provinces));
    }

    @Test
    public void alphabeticalNumbering() {
        Xml provinces = new Xml() {
            @Override
            protected void doBuild() {
                ae("provinces");
                ae(L, e("name", t("Alberta")));
                ae(L, e("name", t("Manitoba")));
                ae(L, e("name", t("Ontario")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::provinces));
                ae(L, get(this::name));
            }

            void provinces() {
                ae(xsTemplate("provinces"));
                ae(L, xsApply("name"));
            }

            void name() {
                ae(xsTemplate("name"));
                ae(L, xsNumber(" a. "), xsValue(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" a. Alberta");
                l(" b. Manitoba");
                l(" c. Ontario");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(provinces));
    }

    @Test
    public void number() {
        Xml provinces = new Xml() {
            @Override
            protected void doBuild() {
                ae("provinces");
                ae(L, e("name", t("Alberta")));
                ae(L, e("name", t("Manitoba")));
                ae(L, e("name", t("Ontario")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::provinces));
                ae(L, get(this::name));
            }

            void provinces() {
                ae(xsTemplate("provinces"));
                ae(L, xsApply("name"));
            }

            void name() {
                ae(xsTemplate("name"));
                ae(L, xsNumber(" 1. "), xsValue(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l(" 1. Alberta");
                l(" 2. Manitoba");
                l(" 3. Ontario");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(provinces));
    }

    @Test
    public void position() {
        Xml provinces = new Xml() {
            @Override
            protected void doBuild() {
                ae("provinces");
                ae(L, e("name", t("Alberta")));
                ae(L, e("name", t("Manitoba")));
                ae(L, e("name", t("Ontario")));
            }
        }.build();

        Xsl xsl = new Xsl() {
            @Override
            protected void doBuild() {
                ae(xslStyleSheetV1(xsOutputText()));
                ae(L, get(this::provinces));
                ae(L, get(this::name));
            }

            void provinces() {
                ae(xsTemplate("provinces"));
                ae(L, xsApply("name"));
            }

            void name() {
                ae(xsTemplate("name"));
                ae(L, xsValue("position()"), xsText(". "));
                ae(L, xsValue(), xsLine());
            }
        }.build();

        Str expected = new Str() {
            @Override
            protected void doBuild() {
                l("1. Alberta");
                l("2. Manitoba");
                l("3. Ontario");
            }
        }.build();

        assertEquals(expected.get(), Tr.xsl(xsl).apply(provinces));
    }

}