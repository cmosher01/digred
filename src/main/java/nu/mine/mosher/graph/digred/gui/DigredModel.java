package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.DigraphSchema;
import org.neo4j.driver.Record;

import java.util.List;

public class DigredModel {
    public final DigraphSchema schema;

    public int iVertexCurr = -1;
    public int iVertexNext = 0;

    public List<Record> listResults;

    public int iEntityCurr = -1;
    public int iEntityNext = -1;
    public int idEntityNext = -1;

    public String search = "";

    public DigredModel(final DigraphSchema schema) {
        this.schema = schema;
    }
}
