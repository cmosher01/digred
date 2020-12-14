package nu.mine.mosher.graph.digred.schema;

import java.util.List;

public interface Entity {
    List<Prop> props();
    boolean vertex();
    boolean common();
    Entity withExtraProps(List<Prop> props);
    String display();
    String typename();
}
