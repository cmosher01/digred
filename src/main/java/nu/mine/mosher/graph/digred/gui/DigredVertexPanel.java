package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class DigredVertexPanel extends Panel {
    private static final Logger LOG = LoggerFactory.getLogger(DigredVertexPanel.class);

    private final DigredModel model;
    private final DataStore datastore;

    private Choice choiceVertex;
    private java.awt.List listboxResults;
    private Button buttonNew;

    public DigredVertexPanel(final DigredModel model, final DataStore dataStore) {
        this.model = model;
        this.datastore = dataStore;
    }

    public void init() {
        final GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        final GridBagConstraints howToLayOut = new GridBagConstraints();
        howToLayOut.insets = new Insets(5,5,5,5);
        howToLayOut.gridx = 0;

        this.choiceVertex = new Choice();
        this.model.schema.e().forEach(v -> this.choiceVertex.add(v.display()));
        this.choiceVertex.addItemListener(this::selectedVertex);
        howToLayOut.gridy = 0;
        howToLayOut.anchor = GridBagConstraints.PAGE_START;
        layout.setConstraints(this.choiceVertex, howToLayOut);
        howToLayOut.gridy = GridBagConstraints.RELATIVE;
        howToLayOut.anchor = GridBagConstraints.CENTER;
        add(this.choiceVertex);

        this.listboxResults = new java.awt.List();
        this.listboxResults.addActionListener(this::selectedEntity);
        howToLayOut.weightx = 1.0D;
        howToLayOut.weighty = 1.0D;
        howToLayOut.fill = GridBagConstraints.BOTH;
        layout.setConstraints(this.listboxResults, howToLayOut);
        howToLayOut.weightx = 0.0D;
        howToLayOut.weighty = 0.0D;
        howToLayOut.fill = GridBagConstraints.NONE;
        add(this.listboxResults);

        this.buttonNew = new Button("New");
        this.buttonNew.addActionListener(this::pressedNew);
        layout.setConstraints(this.buttonNew, howToLayOut);
        add(this.buttonNew);
    }

    private void selectedVertex(final ItemEvent e) {
        this.model.iVertexNext = this.choiceVertex.getSelectedIndex();
        updateViewFromModel();
    }

    private void selectedEntity(final ActionEvent e) {
        this.model.iEntityNext = this.listboxResults.getSelectedIndex();
        updateViewFromModel();
    }

    private void pressedNew(final ActionEvent e) {
        final var vertex = this.model.schema.e().get(this.model.iVertexCurr);

        if (vertex.vertex()) {
            final var cyProps = new ArrayList<String>();
            vertex.props().forEach(prop -> {
                switch (prop.key()) {
                    case "_digred_pk" -> cyProps.add("pk: apoc.create.uuid()");
                    case "_digred_version" -> cyProps.add("version: 1");
                    case "_digred_created" -> cyProps.add("created: datetime.transaction()");
                    case "_digred_modified" -> cyProps.add("modified: datetime.transaction()");
                }
            });

            final var query = new Query(String.format(
                "CREATE (:%s { %s })",
                vertex.typename(),
                String.join(",", cyProps)));

            try (final var session = datastore.session()) {
                session.writeTransaction(tx -> tx.run(query).consume());
            }

            this.model.iVertexCurr = -1;
            updateViewFromModel();
        }
    }

    public void updateViewFromModel() {
        this.choiceVertex.select(this.model.iVertexNext);
        if (this.model.iVertexCurr != this.model.iVertexNext) {
            queryEntities(this.model.schema.e().get(this.model.iVertexNext));
            this.model.iVertexCurr = this.model.iVertexNext;
        }

        if (0 <= this.model.iEntityNext && this.model.iEntityNext < this.model.listResults.size()) {
            this.listboxResults.select(this.model.iEntityNext);
            this.listboxResults.makeVisible(this.model.iEntityNext);
            if (this.model.iEntityCurr != this.model.iEntityNext) {
                queryEntity(this.model.listResults.get(this.model.iEntityNext));
                this.model.iEntityCurr = this.model.iEntityNext;
            }
        }

        this.buttonNew.setEnabled(this.model.schema.e().get(this.model.iVertexCurr).vertex());
    }

    private void queryEntities(final Entity vertex) {
        System.err.println("QUERY AND DISPLAY table: "+vertex.typename());
        final Query query;
        if (this.model.search.isBlank()) {
            query = new Query(String.format("MATCH (n:%s) " +
                    "RETURN n {.pk, .modified, .name }, ID(n) AS id " +
                    "ORDER BY n.modified DESC "+
                    "LIMIT 100",
                vertex.typename()));
        } else {
            // TODO google-style full-text search
            query = new Query("TODO");
        }

        final java.util.List<Record> rs;
        if (vertex.vertex()) {
            try (final var session = datastore.session()) {
                rs = session.readTransaction(tx -> tx.run(query).list());
            }
        } else {
            // TODO
            rs = Collections.emptyList();
        }

        this.model.listResults = new ArrayList<>();
        this.listboxResults.removeAll();
        rs.forEach(r -> {
            this.model.listResults.add(r);
            this.listboxResults.add(resultDisplayNameOf(vertex.typename(), r));
        });

        if (!this.model.listResults.isEmpty()) {
            EventQueue.invokeLater(() -> this.listboxResults.requestFocus());
        }

        this.model.iEntityCurr = -1;
        this.model.iEntityNext = 0;
        preSelectList();

        validate();
    }

    private void preSelectList() {
        // TODO
    }

    private static String resultDisplayNameOf(final String typename, final Record r) {
        // TODO fix display name
        final Value mod = r.get("n").asMap(Values.ofValue()).get("modified");
        final String ts = Objects.isNull(mod) ? "<null>" : TypeConstructor.DATE_TIME.covers(mod) ? mod.asZonedDateTime().toString() : ""+mod.asObject();
        return r.get("name", "unnamed "+typename) + ", " +
            "modified="+ ts +", " +
            "ID=" + r.get("id") + "";
    }

    private void queryEntity(final Record r) {
        System.err.println("QUERY AND DISPLAY object: "+r.get("id"));
    }
}
