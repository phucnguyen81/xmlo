package org.lo.xml;

import static org.junit.Assert.*;

import org.junit.Test;

import org.lo.xml.Str;

public class StrTest {

    @Test
    public void buildXml() {
        String expected = "<company>" +
            "<staff id=\"1\">" +
            "<firstname>yong</firstname>" +
            "<lastname>mook kim</lastname>" +
            "<nickname>mkyong</nickname>" +
            "<salary>100000</salary>" +
            "</staff>" +
            "</company>";
        String actual = new Str() {
            @Override
            protected void doBuild() {
                a("<company>");
                a(L, "<staff id=\"1\">");
                a(L, L, "<firstname>yong</firstname>");
                a(L, L, "<lastname>mook kim</lastname>");
                a(L, L, "<nickname>mkyong</nickname>");
                a(L, L, "<salary>100000</salary>");
                a(L, "</staff>");
                a("</company>");
            }
        }.make();

        assertEquals(expected, actual);
    }

}
