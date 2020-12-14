package nu.mine.mosher.graph.digred.schema;

import java.util.*;

// (tail)-[:type{props}]->(head)
public record Edge (
    String type,
    List<Prop> props,
    Vertex tail,
    Vertex head
)
implements Entity {
    @Override
    public boolean common() {
        return false;
    }

    @Override
    public Edge withExtraProps(List<Prop> props) {
        props = new ArrayList<>(props);
        props.addAll(props());
        props = List.copyOf(props);
        return new Edge(type(), props, tail(), head());
    }

    @Override
    public String display() {
        return
            tail().display()+
            "-[:"+type()+"]->"
            +head().display();
    }

    @Override
    public boolean vertex() {
        return false;
    }

    @Override
    public String typename() {
        return type();
    }
}
