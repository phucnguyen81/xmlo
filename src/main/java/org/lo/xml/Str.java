package org.lo.xml;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Build string, especially tree-like strings like xml, json, ect.
 *
 * @author phuc
 */
public abstract class Str implements Supplier<String> {

    /** For indentation */
    public static final Object L = new Object();

    /** String used for indentation */
    private CharSequence indent = "";

    /** String used for line-separator */
    private CharSequence lineSeparator = System.lineSeparator();

    private final StringBuilder sb = new StringBuilder();

    /** Quick instatiation without using {{@link #doBuild()} */
    public static Str of(Object... args) {
        return new Str() {
            @Override
            protected void doBuild() {}
        }.a(args);
    }

    /** Instantiate with args as initial value */
    public Str(Object... args) {
        a(args);
    }

    /**
     * Template for calling methods like a() or l() to build the string.
     */
    protected abstract void doBuild();

    /** Build then return 'this' for chaining. */
    public Str build() {
        doBuild();
        return this;
    }

    /** Build then return the result string */
    public String make() {
        return build().get();
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public String get() {
        return sb.toString();
    }

    public String format(Object... args) {
        return String.format(sb.toString(), args);
    }

    /** Append args as strings */
    public Str a(Object... args) {
        Arrays.stream(args).map(a -> a == L ? this.indent : a)
            .forEach(sb::append);
        return this;
    }

    /** Append args plus a line-separator to the end */
    public Str l(Object... args) {
        return a(args).a(lineSeparator);
    }

    /** Set the string used for indentation */
    public Str indent(CharSequence s) {
        this.indent = s;
        return this;
    }

    /** Set number of spaces for indentation */
    public Str indentSize(int size) {
        return indent(IntStream.range(0, size).mapToObj(i -> " ")
            .collect(Collectors.joining()));
    }

    /** String used for indentation */
    public Str lineSep(CharSequence s) {
        this.lineSeparator = s;
        return this;
    }

}