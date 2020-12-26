package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.*;

import java.util.List;
import java.util.Map;

public class DigredModel {
    public final DigraphSchema schema;
    public final Map<Vertex, List<Edge>> edgesOut;
    public final Map<Vertex, List<Edge>> edgesIn;

    public DigredModel(final DigraphSchema schema) {
        this.schema = schema;
        this.edgesIn = schema.edgesIn();
        this.edgesOut = schema.edgesOut();
//        this.typeNext = schema.e().get(0);
    }



//    public Entity typeCurr;
//    public Entity typeNext;
//
//    public Entity type() {
//        return this.typeNext;
//    }
//
//
//
//    public long iEntityCurr = -1;
//    public long idEntityNext = -1;
//
//    public boolean isSelectedEntity() {
//        return 0 <= this.idEntityNext;
//    }
//
//    public Long idEntity() {
//        return this.idEntityNext;
//    }
}
