package nu.mine.mosher.graph.digred.schema;

import java.util.*;
import java.util.stream.Collectors;

public record DigraphSchema (
    List<Entity> e
) {
    public static final String DIGRED_COMMON = "_digred_common";

//    public void decompile(final PrintWriter out) {
//        v.forEach(x -> {
//            out.println(x.label());
//            x.props().forEach(p -> {
//                out.print("    ");
//                out.print(p.key());
//                out.print(" : ");
//                out.println(p.type());
//            });
//        });
//        out.println();
//        e.forEach(x -> {
//            out.print(x.tail().label());
//            out.print(" ");
//            out.print(x.type());
//            out.print(" ");
//            out.println(x.head().label());
//            x.props().forEach(p -> {
//                out.print("    ");
//                out.print(p.key());
//                out.print(" : ");
//                out.println(p.type());
//            });
//        });
//    }

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
