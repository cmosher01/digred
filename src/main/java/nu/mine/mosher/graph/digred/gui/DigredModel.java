package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.*;

import java.util.*;

public class DigredModel {
    public final DigraphSchema schema;
    public final Map<Vertex, List<Edge>> edgesOut;
    public final Map<Vertex, List<Edge>> edgesIn;

    public DigredModel(final DigraphSchema schema) {
        this.schema = schema;
        this.edgesIn = schema.edgesIn();
        this.edgesOut = schema.edgesOut();
    }
}
