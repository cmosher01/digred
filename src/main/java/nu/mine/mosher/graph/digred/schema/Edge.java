package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;

// (tail)-[:type{props}]->(head)
public class Edge implements Entity {
    private final String type;
    private final List<Prop> props;
    private final Vertex tail;
    private final Vertex head;

    public Edge(String type, List<Prop> props, Vertex tail, Vertex head) {
        this.type = type;
        this.props = props;
        this.tail = tail;
        this.head = head;
    }

    public String type() {
        return this.type;
    }

    public List<Prop> props() {
        return this.props;
    }

    public Vertex tail() {
        return this.tail;
    }

    public Vertex head() {
        return this.head;
    }

    @Override
    public boolean common() {
        return false;
    }

    @Override
    public void addExtraProps(List<Prop> props) {
        props().addAll(props);
    }

    @Override
    public boolean vertex() {
        return false;
    }

    @Override
    public String typename() {
        return type();
    }

    @Override
    public void decompile(final PrintWriter out) {
        tail().decompile(out);
        out.print(" ");
        out.print(typename());
        out.print(" ");
        head().decompile(out);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Edge)) {
            return false;
        }
        final Edge that = (Edge)object;
        return
            type().equals(that.type()) &&
            tail().equals(that.tail()) &&
            head().equals(that.head());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), tail(), head());
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
