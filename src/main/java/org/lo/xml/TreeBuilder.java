package org.lo.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Base class for building trees in a visual manner.
 *
 * @param <N> type of tree to build.
 *
 * @author phuc
 */
public abstract class TreeBuilder<N> implements Supplier<N>, Iterable<N> {

    /** Represent indentation/depth */
    public static final Object L = new Object();

    /**
     * Map from element depth to nodes at that depth, in their insertion order.
     * This captures the state of this builder. There is a single root at level
     * 0. Other nodes are added by calling {@link #add(Object...)}.
     */
    private final TreeMap<Integer, LinkedList<N>> nodes;

    /** Create an builder with null root base */
    public TreeBuilder() {
        this(null);
    }

    public TreeBuilder(N root) {
        nodes = new TreeMap<>();
        reset(root);
    }

    /** Attempt to add child to parent */
    protected abstract void addChild(N parent, N child);

    /** Return the nodes created from processing the arguments. */
    protected abstract Iterable<N> parseArguments(Iterable<?> args);

    /**
     * Add nodes to be built.
     *
     * @see #parseArguments(Object[]) for how arguments are handled
     */
    public void add(Iterable<?> args) {
        int depth = 1;
        List<Object> ls = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                throw new NullPointerException(
                    "Found null in " + Arrays.asList(args));
            } else if (arg == L) {
                depth += 1;
            } else {
                ls.add(arg);
            }
        }
        for (N node : parseArguments(ls)) {
            registerNode(node, depth);
        }
    }

    /** Reset the builder to build another tree given its root */
    public void reset(N root) {
        nodes.clear();
        registerNode(root, 0);
    }

    /** Reset the builder to build another tree */
    public void reset() {
        reset(null);
    }

    @Override
    public String toString() {
        return nodes.toString();
    }

    /**
     * Get the first base added to root, which represents the tree being built.
     * Return null if no nodes have been added.
     */
    @Override
    public N get() {
        return children().stream().findFirst().orElse(null);
    }

    @Override
    public Iterator<N> iterator() {
        return children().iterator();
    }

    /**
     * Get the first children added to root.
     */
    public List<N> children() {
        Integer depth = nodes.higherKey(0);
        if (depth != null && nodes.containsKey(depth)) {
            return new ArrayList<>(nodes.get(depth));
        } else {
            return Collections.emptyList();
        }
    }

    public N root() {
        return nodes.get(0).peekFirst();
    }

    /** Link the base just added to to tree being built */
    private void registerNode(N node, int depth) {
        // keep track of base's depth to find its parent
        if (!nodes.containsKey(depth)) {
            nodes.put(depth, new LinkedList<N>());
        }
        nodes.get(depth).addLast(node);

        // link element to its parent
        N parent = findParent(depth);
        if (parent != null) {
            addChild(parent, node);
        }
    }

    /**
     * Find parent base of the last inserted base. Knowning the base's depth is
     * enough to find its parent: the parent is the last inserted base that has
     * depth smaller than the child depth.
     */
    private N findParent(Integer childDepth) {
        if (childDepth < 0) {
            throw new IllegalArgumentException(
                "Expect non-negative tree depth");
        }
        Integer parentDepth = nodes.lowerKey(childDepth);
        if (parentDepth == null) {
            return null;
        }
        LinkedList<N> parents = nodes.get(parentDepth);
        return parents.peekLast();
    }

}