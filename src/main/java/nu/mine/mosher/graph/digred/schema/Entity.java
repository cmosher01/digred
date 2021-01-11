package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;

public interface Entity {
    List<Prop> props();
    boolean vertex();
    boolean common();
    void addExtraProps(List<Prop> props);
    String display();
    String typename();
    void decompile(PrintWriter writer);
    Optional<Prop> propOf(DataType dataType);
}
