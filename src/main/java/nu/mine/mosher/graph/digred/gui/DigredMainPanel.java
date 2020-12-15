package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;

public class DigredMainPanel extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DigredMainPanel.class);

    private final DigredModel model;
    private final DataStore datastore;
    private DigredVertexPanel panelVertex;
    private DigredPropsPanel panelProps;



    public static DigredMainPanel create(final DigredModel model, final DataStore dataStore) {
        final DigredMainPanel panel = new DigredMainPanel(model, dataStore);
        panel.init();
        return panel;
    }

    private DigredMainPanel(final DigredModel model, final DataStore dataStore) {
        this.model = model;
        this.datastore = dataStore;
    }

    public void init() {
        setLayout(new GridLayout(1,2));
        setBackground(Color.CYAN); // TODO remove MAIN PANEL CYAN

        this.panelVertex = DigredVertexPanel.create(this.model, this.datastore);
        this.panelVertex.setActionListener(this::onEntityChosen);
        this.panelVertex.setBackground(Color.MAGENTA); // TODO remove LEFT-SIDE PANEL MAGENTA
        add(this.panelVertex);

        this.panelProps = DigredPropsPanel.create(this.model, this.datastore);
        this.panelProps.setBackground(Color.YELLOW); // TODO remove RIGHT-SIDE PANEL YELLOW
        add(this.panelProps);
    }

    public void updateViewFromModel() {
        this.panelVertex.updateViewFromModel();
        this.panelProps.updateViewFromModel();
    }

    private void onEntityChosen(ActionEvent e) {
        this.panelProps.updateViewFromModel();
    }
}
