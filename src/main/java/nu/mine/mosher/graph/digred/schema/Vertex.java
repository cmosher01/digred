package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;

// (:label{props})
public record Vertex (
    String label,
    List<Prop> props
)
implements Entity {
    @Override
    public boolean common() {
        return DigraphSchema.common(label());
    }

    @Override
    public void addExtraProps(List<Prop> props) {
        props().addAll(props);
    }

    @Override
    public String display() {
        return display("");
    }

    public String display(final String name) {
        return "("+name+":"+label()+")";
    }

    public String display(final long id) {
        return "(:"+label()+"{ID:"+id+"})";
    }

    @Override
    public boolean vertex() {
        return true;
    }

    @Override
    public String typename() {
        return label();
    }

    @Override
    public void decompile(final PrintWriter out) {
        out.print(typename());
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Vertex that)) {
            return false;
        }
        return label().equals(that.label());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label());
    }

    @Override
    public Optional<Prop> propOf(final DataType dataType) {
        for (final Prop p : props()) {
            if (p.type() == dataType) {
                return Optional.of(p);
            }
        }
        return  Optional.empty();
    }
}
