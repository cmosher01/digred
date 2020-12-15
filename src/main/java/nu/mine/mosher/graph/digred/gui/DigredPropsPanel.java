package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.types.Node;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.*;
import java.util.*;

public class DigredPropsPanel extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DigredPropsPanel.class);

    private final DigredModel model;
    private final DataStore datastore;
    private Label label;
    private Panel holderProps;

    public static DigredPropsPanel create(final DigredModel model, final DataStore datastore) {
        final DigredPropsPanel panel = new DigredPropsPanel(model, datastore);
        panel.init();
        return panel;
    }

    public void init() {
        setLayout(new GridLayout(3,1));
        this.label = new Label();
        this.label.setBackground(Color.GREEN); // TODO
        add(this.label);

        add(new Panel());
    }

    public void updateViewFromModel() {
        if (0 <= this.model.iEntityNext) {
            final Record rec = this.model.listResults.get(this.model.iEntityNext);

            final Entity vertex = this.model.schema.e().get(this.model.iVertexNext);
            final Query query;
            if (vertex.vertex()) {
                query = new Query(String.format("MATCH (n:%s) WHERE ID(n) = $id RETURN n",
                    vertex.typename()),
                    Map.of("id", rec.get("id").asLong()));
            } else {
                // TODO query for relationship
                query = null;
            }

            final Record rs;
            try (final var session = datastore.session()) {
                rs = session.readTransaction(tx -> tx.run(query).single());
            }

            // label (for debugging only)
            final Node node = rs.get("n").asNode();
            this.label.setText(vertex.typename()+" [pk="+ node.get("pk").asString()+"]");



            if (Objects.nonNull(this.holderProps)) {
                remove(this.holderProps);
            }
            this.holderProps = new Panel();
            this.holderProps.setBackground(Color.ORANGE);
            final var props = vertex.props();
            this.holderProps.setLayout(new GridLayout(props.size(),2,5,5));
            props.forEach(prop -> {
                final String key = filterDigredKeyName(prop.key());
                final Label labelProp = new Label(key);
                this.holderProps.add(labelProp);
                final TextField stringProp = new TextField(displayValueOf(node.get(key)));
                stringProp.setColumns(60);
                stringProp.setEditable(false);
                this.holderProps.add(stringProp);
            });
            add(this.holderProps);
        } else {
            this.label.setText("[no entity]");
            if (Objects.nonNull(this.holderProps)) {
                remove(this.holderProps);
                this.holderProps = null;
            }
        }
        validate();
    }

    private String filterDigredKeyName(String key) {
        if (key.startsWith("_digred_")) {
            return key.substring(8);
        }
        return key;
    }

    private String displayValueOf(final Value value) {
        if (TypeConstructor.STRING.covers(value)) {
            return value.asString();
        }
        if (TypeConstructor.INTEGER.covers(value)) {
            return Long.toString(value.asLong(), 10);
        }
        if (TypeConstructor.FLOAT.covers(value)) {
            return Double.toString(value.asDouble());
        }
        if (TypeConstructor.DATE_TIME.covers(value)) {
            return value.asZonedDateTime().toString();
        }
        if (TypeConstructor.NULL.covers(value)) {
            return "[null]";
        }
        return "[cannot convert value for display]";
    }

    private DigredPropsPanel(DigredModel model, DataStore datastore) {
        this.model = model;
        this.datastore = datastore;
    }
}
