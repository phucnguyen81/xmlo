package org.lo.xml;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import org.lo.xml.TreeBuilder;

public class TreeBuilderTest {

    @Test
    public void treeShouldBeNullIfNotBuilt() {
        assertNull(new TreeBuilder<Object>(new Object()) {
            @Override
            protected void addChild(Object parent, Object child) {}

            @Override
            protected Iterable<Object> parseArguments(Iterable<?> args) {
                return Collections.emptyList();
            }
        }.get());
    }
}
