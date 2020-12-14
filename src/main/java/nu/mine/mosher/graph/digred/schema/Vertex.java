package nu.mine.mosher.graph.digred.schema;

import java.util.*;

// (:label{props})
public record Vertex (
    String label,
    List<Prop> props
)
implements Entity {
    @Override
    public boolean common() {
        return label().equals(DigraphSchema.DIGRED_COMMON);
    }

    @Override
    public Vertex withExtraProps(List<Prop> props) {
        props = new ArrayList<>(props);
        props.addAll(props());
        props = List.copyOf(props);
        return new Vertex(label(), props);
    }

    @Override
    public String display() {
        return "(:"+label()+")";
    }

    @Override
    public boolean vertex() {
        return true;
    }

    @Override
    public String typename() {
        return label();
    }
}
