package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static nu.mine.mosher.graph.digred.gui.DigredPropsForm.displayValueOf;

public class DigredVertexPanel extends Panel implements ViewUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DigredVertexPanel.class);

    private final DigredModel model;
    private final DataStore datastore;
    private final ViewUpdater updater;

    private Choice choiceVertex;
    private java.awt.List listboxResults;
    private java.util.List<Record> listResults;
    private Button buttonNew;

    private DigredEntityIdent ident;

    public static DigredVertexPanel create(final DigredModel model, final DataStore dataStore, final ViewUpdater updater) {
        Tracer.trace("DigredVertexPanel: create");
        final DigredVertexPanel panel = new DigredVertexPanel(model, dataStore, updater);
        panel.init();
        return panel;
    }

    private DigredVertexPanel(final DigredModel model, final DataStore dataStore, final ViewUpdater updater) {
        this.model = model;
        this.datastore = dataStore;
        this.updater = updater;
    }

    public void init() {
        final GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        final GridBagConstraints lay = new GridBagConstraints();
        lay.insets = new Insets(5,5,5,5);
        lay.gridx = 0;

        this.choiceVertex = new Choice();
        this.model.schema.e().forEach(v -> this.choiceVertex.add(v.display()));
        this.choiceVertex.addItemListener(this::selectedVertex);
        layout.setConstraints(this.choiceVertex, lay);
        add(this.choiceVertex);

        this.listboxResults = new java.awt.List();
        this.listboxResults.addActionListener(this::selectedEntity);
        lay.weightx = 1.0D;
        lay.weighty = 1.0D;
        lay.fill = GridBagConstraints.BOTH;
        layout.setConstraints(this.listboxResults, lay);
        lay.weightx = 0.0D;
        lay.weighty = 0.0D;
        lay.fill = GridBagConstraints.NONE;
        add(this.listboxResults);

        this.buttonNew = new Button("New");
        this.buttonNew.addActionListener(this::pressedNew);
        layout.setConstraints(this.buttonNew, lay);
        add(this.buttonNew);
    }

    @Override
    public void updateViewFromModel(final DigredEntityIdent ident) {
        Tracer.trace("DigredVertexPanel: updateViewFromModel");
        Tracer.trace("    ident: "+ident);

        this.ident = ident;

        this.choiceVertex.select(iTypeOf(this.ident.type()));
        queryEntities();

        this.buttonNew.setEnabled(this.ident.type().vertex());
    }

    // User command: choose an entity type from drop down
    private void selectedVertex(final ItemEvent e) {
        Tracer.trace("SELECT TYPE: "+e.paramString());
        final var type = this.model.schema.e().get(this.choiceVertex.getSelectedIndex());
        this.ident = new DigredEntityIdent(type);
        updateViewFromModel(this.ident);
    }

    // User command: choose an entity from the list box
    private void selectedEntity(final ActionEvent e) {
        Tracer.trace("SELECT ENTITY: "+e.paramString());
        final var record = this.listResults.get(this.listboxResults.getSelectedIndex());
        final var id = record.get("id").asLong();
        this.ident = this.ident.with(id);
        this.updater.updateViewFromModel(ident);
    }

    // User command: pressed "New" button to create a new entity
    private void pressedNew(final ActionEvent e) {
        Tracer.trace("NEW: "+e.paramString());
        final var entity = this.ident.type();
        if (entity.vertex()) {
            final var cyProps = new ArrayList<String>();
            entity.props().forEach(prop -> {
                switch (prop.key()) {
                    case "_digred_pk" -> cyProps.add("pk: apoc.create.uuid()");
                    case "_digred_version" -> cyProps.add("version: 1");
                    case "_digred_created" -> cyProps.add("created: datetime.realtime()");
                    case "_digred_modified" -> cyProps.add("modified: datetime.realtime()");
                }
            });

            final var query = new Query(String.format(
                "CREATE (n:%s { %s }) RETURN ID(n) as id",
                entity.typename(),
                String.join(",", cyProps)));

            final Record rec;
            try (final var session = datastore.session()) {
                Tracer.trace(query.toString());
                rec = session.writeTransaction(tx -> tx.run(query).single());
            }

            updateViewFromModel(this.ident.with(rec.get("id").asLong()));
        }
    }



    private int iTypeOf(Entity type) {
        int i = 0;
        for (final var v : this.model.schema.e()) {
            if (v.equals(type)) {
                return i;
            }
            ++i;
        }
        return 0;
    }

    // fill the list box with entities of the currently chosen entity type (with the drop down)
    private void queryEntities() {
        final var entity = this.ident.type();

        final Query query;

        if (entity.vertex()) {
            final Vertex vertex = (Vertex)entity;
//            if (this.model.search.isBlank()) {
//            "CASE exists(n.name) WHEN true THEN n.name ELSE n.modified+' '+labels(n)[0]+'['+ID(n)+']' END";
                query = new Query(
                    String.format(
                        "MATCH (n:%s) " +
                        "RETURN n {.pk, .modified, .name }, ID(n) AS id " +
                        "ORDER BY n.modified DESC " +
                        "LIMIT 100",
                    vertex.typename()));
//            } else {
                // TODO google-style full-text search
//                query = new Query("TODO");
//            }
        } else {
            final Edge edge = (Edge)entity;
            query = new Query(
                String.format(
                    "MATCH (tail:%s)-[n:%s]->(head:%s) "+
                    "RETURN " +
                        "tail {.pk, .modified, .name}, ID(tail) AS idTail, " +
                        "head {.pk, .modified, .name}, ID(head) AS idHead, " +
                        "n {.pk, .modified, .name}, ID(n) AS id "+
                    "ORDER BY n.modified DESC "+
                    "LIMIT 100",
                edge.tail().typename(),
                edge.typename(),
                edge.head().typename()));
        }

        final java.util.List<Record> rs;
        try (final var session = datastore.session()) {
            Tracer.trace(query.toString());
            rs = session.readTransaction(tx -> tx.run(query).list());
            Tracer.trace("records found: "+rs.size());
        }

        this.listResults = new ArrayList<>();
        this.listboxResults.removeAll();
        long idFirst = -1;
        int preselect = -1;
        for (final var r : rs) {
            final var id = r.get("id").asLong();

            this.listResults.add(r);
            this.listboxResults.add(resultDisplayNameOf(entity, r));

            if (idFirst < 0) {
                idFirst = id;
            }
            if (this.ident.id().isPresent()) {
                if (id == this.ident.id().get()) {
                    preselect = this.listResults.size() - 1;
                }
            }
        }

        if (this.listResults.isEmpty()) {
            this.updater.updateViewFromModel(new DigredEntityIdent(this.ident.type()));
        } else {
            if (this.ident.id().isPresent() && 0 <= preselect) {
                this.listboxResults.select(preselect);
                this.listboxResults.makeVisible(preselect);
            } else {
                if (this.ident.id().isPresent()) {
                    LOG.warn("Could not locate requested ID in list: "+this.ident.id().get());
                }
                Tracer.trace("selecting first item in list, with ID="+idFirst);
                this.ident = this.ident.with(idFirst);
                this.listboxResults.select(0);
                this.listboxResults.makeVisible(0);
            }
            this.updater.updateViewFromModel(this.ident);

            EventQueue.invokeLater(() -> this.listboxResults.requestFocus());
        }
    }

    public static String resultDisplayNameOf(final Entity entity, final Record r) {
        if (entity.vertex()) {
            final var e = (Vertex)entity;
            final var props = r.get("n").asMap(Values.ofValue());
            var ts = displayValueOf(props.get("modified"));
            if (!ts.isEmpty()) {
                ts = ts+": ";
            }

            var name = props.get("name");
            if (Objects.nonNull(name) && !name.isNull() && !name.isEmpty()) {
                return name.asString();
            }
            return ts+e.display(r.get("id").asLong());
        } else {
            final var e = (Edge)entity;
            final var props = r.get("n").asMap(Values.ofValue());

            final String ts;
            final String rel;
            var name = props.get("name");
            if (Objects.nonNull(name) && !name.isNull() && !name.isEmpty()) {
                ts = "";
                rel = name.asString();
            } else {
                ts = displayValueOf(props.get("modified"))+": ";
                rel = e.display(r.get("id").asLong());
            }

            final String tail;
            {
                final var propsT = r.get("tail").asMap(Values.ofValue());
                var nameT = propsT.get("name");
                if (Objects.nonNull(nameT) && !nameT.isNull() && !nameT.isEmpty()) {
                    tail = nameT.asString();
                } else {
                    tail = e.tail().display(r.get("idTail").asLong());
                }
            }
            final String head;
            {
                final var propsT = r.get("head").asMap(Values.ofValue());
                var nameT = propsT.get("name");
                if (Objects.nonNull(nameT) && !nameT.isNull() && !nameT.isEmpty()) {
                    head = nameT.asString();
                } else {
                    head = e.head().display(r.get("idHead").asLong());
                }
            }

            return ts+tail+" - "+rel+" -> "+head;
        }
    }
}
