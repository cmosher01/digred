package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;

// (:label{props})
public class Vertex implements Entity {
    private final String label;
    private final List<Prop> props;

    public Vertex(String label, List<Prop> props) {
        this.label = label;
        this.props = List.copyOf(props);
    }

    public String label() {
        return this.label;
    }

    public List<Prop> props() {
        return this.props;
    }

    @Override
    public boolean common() {
        return DigraphSchema.common(label());
    }

    @Override
    public void addExtraProps(List<Prop> props) {
        props().addAll(props);
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
        if (!(object instanceof Vertex)) {
            return false;
        }
        final Vertex that = (Vertex)object;
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
