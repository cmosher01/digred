package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DigredMainPanel extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DigredMainPanel.class);

    private final DigredModel model;
    private final DataStore datastore;


    private GridBagLayout layout;
    private DigredVertexPanel panelVertex;
    private DigredPropsPanel panelProps; // TODO
    private Panel panelProp;

    public DigredMainPanel(final DigredModel model, final DataStore dataStore) {
        this.model = model;
        this.datastore = dataStore;
    }

    public void init() {
        this.layout = new GridBagLayout();
        setLayout(this.layout);
//        setBackground(Color.CYAN); // TODO

        this.panelVertex = new DigredVertexPanel(this.model, this.datastore);
        this.panelVertex.init();
        this.panelVertex.updateViewFromModel();

        final GridBagConstraints howToLayOut = new GridBagConstraints();
        howToLayOut.gridx = 0;
        howToLayOut.weightx = 1.0D / 2.0D;
        howToLayOut.gridy = 0;
        howToLayOut.weighty = 1.0D;
        howToLayOut.fill = GridBagConstraints.BOTH;
        this.layout.setConstraints(this.panelVertex, howToLayOut);
//        this.panelVertex.setBackground(Color.MAGENTA); // TODO
        add(this.panelVertex);

        updateViewFromModel();
    }


    public void updateViewFromModel() {
        final List<Prop> props;
        if (0 <= this.model.iVertexCurr) {
            props = this.model.schema.e().get(this.model.iVertexCurr).props();
        } else {
            props = Collections.emptyList();
        }

        if (!props.isEmpty()) {
            if (Objects.nonNull(this.panelProp)) {
                remove(this.panelProp);
            }
            this.panelProp = new Panel();
//            this.panelProp.setBackground(Color.YELLOW); // TODO
            final Panel p = new Panel(new GridLayout(props.size(),2));

            props.forEach(prop -> {
                final Label labelProp = new Label(prop.key());
                p.add(labelProp);
                final TextField stringProp = new TextField("");
                stringProp.setColumns(60);
                stringProp.setEditable(false);
                p.add(stringProp);
            });
            this.panelProp.add(p);

            final GridBagConstraints howToLayOut = new GridBagConstraints();
            howToLayOut.gridx = 1;
            howToLayOut.weightx = 0.0D;
            howToLayOut.gridy = 0;
            howToLayOut.weighty = 1.0D;
            howToLayOut.fill = GridBagConstraints.BOTH;
            this.layout.setConstraints(this.panelProp, howToLayOut);
            add(this.panelProp);
        }


        validate();
    }
}
