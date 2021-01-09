package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.util.*;

import java.awt.*;

public class DigredMainPanel extends Panel implements ViewUpdater {
    private final Frame owner;
    private final DigredModel model;
    private final DataStore datastore;
    private DigredVertexPanel panelVertex;
    private DigredPropsPanel panelProps;



    public static DigredMainPanel create(final Frame owner, final DigredModel model, final DataStore dataStore) {
        Tracer.trace("DigredMainPanel: create");
        final DigredMainPanel panel = new DigredMainPanel(owner, model, dataStore);
        panel.init();
        return panel;
    }

    private DigredMainPanel(final Frame owner, final DigredModel model, final DataStore dataStore) {
        this.owner = owner;
        this.model = model;
        this.datastore = dataStore;
    }

    public void init() {
        setLayout(new ThirdsLayoutManager());
        setBackground(DigredGui.debugLayout(Color.CYAN));

        this.panelProps = DigredPropsPanel.create(this.owner, this.model, this.datastore, this);
        this.panelProps.setBackground(DigredGui.debugLayout(Color.YELLOW));

        this.panelVertex = DigredVertexPanel.create(this.model, this.datastore, this.panelProps);
        this.panelVertex.setBackground(DigredGui.debugLayout(Color.MAGENTA));

        add(this.panelVertex);
        add(this.panelProps);
    }

    @Override
    public void updateViewFromModel(final DigredEntityIdent ident) {
        Tracer.trace("DigredMainPanel: updateViewFromModel");
        Tracer.trace("    ident: "+ident);
        this.panelVertex.updateViewFromModel(ident);
    }
}
