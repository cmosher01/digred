package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public record DigraphSchema (
    List<Entity> e
) {
    public static final String DIGRED_COMMON = "_digred_common";

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
}
