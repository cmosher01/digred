package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
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
        return display("", "", "");
    }

    public String display(final String nameTail, final String name, final String nameHead) {
        return
            tail().display(nameTail)+
            "-["+name+":"+type()+"]->"
            +head().display(nameHead);
    }

//    public String display(final long idTail, final long id, final long idHead) {
//        return
//            tail().display(idTail)+
//            "-[:"+type()+"{ID:"+id+"}]->"
//            +head().display(idHead);
//    }

    public String display(final String name) {
        return "["+name+":"+type()+"]";
    }

    public String display(final long id) {
        return "[:"+type()+"{ID:"+id+"}]";
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
        if (!(object instanceof Edge that)) {
            return false;
        }
        return
            type().equals(that.type()) &&
            tail().equals(that.tail()) &&
            head().equals(that.head());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), tail(), head());
    }
}
