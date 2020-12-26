package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public record DigraphSchema (
    List<Entity> e
) {
    // TODO
    //     _digred_USE_FIELD
    //
    // where USE is pk, modified, created, version, name
    // and FIELD is the actual field name
    //
    // That is to say, field names are treated as normal
    // names, unless they start with an underscore, in
    // which case they are of the following form:
    //
    //     '_' APP '_' USE '_' FIELD
    //
    // Or...
    //
    // Just make these datatypes: _DIGRED_PK, _DIGRED_NAME, etc.
    //
    public static final String DIGRED_PREFIX = "_digred_";
    public static final String DIGRED_COMMON = DIGRED_PREFIX +"common";

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

    public static String filteredKeyword(final String rawKeyword) {
        return rawKeyword.substring(
            rawKeyword.startsWith(DigraphSchema.DIGRED_PREFIX)
                ? DigraphSchema.DIGRED_PREFIX.length()
                : 0);
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
