package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;

public class Prop {
    private final String key;
    private final DataType type;

    public Prop(String key, DataType type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return this.key;
    }

    public DataType type() {
        return this.type;
    }

    public void decompile(final PrintWriter out) {
        out.print(key());
        out.print(" : ");
        out.print(type());
    }

    public String display() {
        return key()+" : "+type();
    }
}
