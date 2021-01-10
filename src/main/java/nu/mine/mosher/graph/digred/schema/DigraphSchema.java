package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public record DigraphSchema (
    List<Entity> e
) {

    // datatypes that start with _DIGRED_ are handled specially by digred application
    private static final String DIGRED_PREFIX = "_DIGRED_";
    private static final String DIGRED_COMMON = DIGRED_PREFIX +"COMMON";

    public static boolean common(final String label) {
        return label.equalsIgnoreCase(DigraphSchema.DIGRED_COMMON);
    }



    public void decompile(final PrintWriter out) {
        e().forEach(entity -> {
            entity.decompile(out);
            out.println();
            entity.props().forEach(p -> {
                out.print("    ");
                p.decompile(out);
                out.println();
            });
        });
    }

    public DigraphSchema withCommonApplied() {
        final var propsCommon = this.e.
            stream().
            filter(Entity::common).
            findAny().
            map(Entity::props).
            orElse(Collections.emptyList());
        if (propsCommon.isEmpty()) {
            return this;
        }

        final var vs = this.e.
            stream().
            filter(v -> !v.common()).
            map(v -> v.withExtraProps(propsCommon)).
            collect(Collectors.toUnmodifiableList());

        return new DigraphSchema(vs);
    }

    public Map<Vertex,List<Edge>> edgesOut() {
        final var map = new HashMap<Vertex, List<Edge>>();
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                map.put(v, new ArrayList<>());
            }
        }
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                map.get(e.tail()).add(e);
            }
        }
        return Map.copyOf(map);
    }

    public Map<Vertex,List<Edge>> edgesIn() {
        final var map = new HashMap<Vertex, List<Edge>>();
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                map.put(v, new ArrayList<>());
            }
        }
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                map.get(e.head()).add(e);
            }
        }
        return Map.copyOf(map);
    }

    public Edge of(final String type, final String tail, final String head) {
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                if (e.typename().equals(type) && e.tail().typename().equals(tail) && e.head().typename().equals(head)) {
                    return e;
                }
            }
        }
        throw new IllegalStateException("Invalid edge: "+tail+"-"+type+"->"+head);
    }

    public Vertex of(final String label) {
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                if (v.typename().equals(label)) {
                    return v;
                }
            }
        }
        throw new IllegalStateException("Invalid vertex: "+label);
    }
}
