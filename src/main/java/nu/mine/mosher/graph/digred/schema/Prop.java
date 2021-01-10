package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;

public record Prop(
    String key,
    DataType type
) {
    public void decompile(final PrintWriter out) {
        out.print(key());
        out.print(" : ");
        out.print(type());
    }

    public String display() {
        return key()+" : "+type();
    }
}
