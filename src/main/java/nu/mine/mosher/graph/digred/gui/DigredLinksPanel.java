package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.awt.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class DigredLinksPanel extends Container {
    private final Frame owner;
    private final DigredModel model;
    private final DataStore datastore;
    private final ViewUpdater updater;
    private final DigredEntityIdent ident;
    private List listboxLinks;
    private java.util.List<DigredEntityIdent> links;
    private final java.util.List<Button> buttons = new ArrayList<>();

    public static DigredLinksPanel create(final Frame owner, final DigredModel model, final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        Tracer.trace("DigredLinksPanel: create");
        Tracer.trace("    ident: "+ident);
        final DigredLinksPanel panel = new DigredLinksPanel(owner, model, datastore, updater, ident);
        panel.init();
        return panel;
    }

    private DigredLinksPanel(final Frame owner, final DigredModel model, final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        this.owner = owner;
        this.model = model;
        this.datastore = datastore;
        this.updater = updater;
        this.ident = ident;
    }

    private void init() {
        setBackground(DigredGui.debugLayout(Color.PINK));
        final var layout = new GridBagLayout();
        setLayout(layout);

        final var cns = new GridBagConstraints();
        cns.insets = new Insets(5,5,5,5);
        cns.gridx = 0;
        cns.anchor = GridBagConstraints.WEST;

        query();

        this.listboxLinks.addActionListener(this::selectedLink);
        cns.weightx = 1.0D;
        cns.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(this.listboxLinks, cns);
        add(this.listboxLinks);
        cns.weightx = 0.0D;
        cns.fill = GridBagConstraints.NONE;

        final var entity = this.ident.type();

        if (!entity.vertex()) {
            throw new IllegalStateException();
        }

        final var vertex = (Vertex)entity;
        this.model.edgesOut.get(vertex).forEach(e -> {
            final var b = new Button("Add   "+e.display("this", "", ""));
            b.addActionListener(x -> pressedAdd(x, e, false));
            this.buttons.add(b);
            layout.setConstraints(b, cns);
            add(b);
        });

        this.model.edgesIn.get(vertex).forEach(e -> {
            final var b = new Button("Add   "+e.display("", "", "this"));
            b.addActionListener(x -> pressedAdd(x, e, true));
            this.buttons.add(b);
            layout.setConstraints(b, cns);
            add(b);
        });
    }

    private void selectedLink(final ActionEvent e) {
        Tracer.trace("SELECT LINK: "+e.paramString());
        this.updater.updateViewFromModel(this.links.get(this.listboxLinks.getSelectedIndex()));
    }

    private void pressedAdd(final ActionEvent x, final Edge e, final boolean incoming) {
        final var vertexTarget = incoming ? e.tail() : e.head();
        final Optional<Long> id = DigredChoosePopup.run(this.owner, this.datastore, vertexTarget);
        if (id.isPresent()) {
            Tracer.trace("ADD: "+x.paramString());
            createEdge(e, vertexTarget, id.get(), incoming);
        } else {
            Tracer.trace("cancel adding relationship");
        }
    }

    private void createEdge(final Edge e, final Vertex typeEntityThat, final long idEntityThat, final boolean incoming) {
        final var typeEntity = this.ident.type();
        final long idEntity = this.ident.id().get();

        final var cyProps = DataStore.digredCypherProps(e);

        final Map<String,Object> params = Map.of(
            "idTail", incoming ? idEntityThat : idEntity,
            "idHead", incoming ? idEntity : idEntityThat);

        final var query = new Query(
            String.format(
                "MATCH (tail:%s), (head:%s) " +
                "WHERE ID(tail) = $idTail AND ID(head) = $idHead " +
                "CREATE (tail)-[r:%s { %s }]->(head) " +
                "RETURN ID(r) as id",
                incoming ? typeEntityThat.typename() : typeEntity.typename(),
                incoming ? typeEntity.typename() : typeEntityThat.typename(),
                e.typename(),
                String.join(",", cyProps)),
            params);

        final Record rec;
        try (final var session = datastore.session()) {
            Tracer.trace(query.toString());
            rec = session.writeTransaction(tx -> tx.run(query).single());
        }
        final var idNew = rec.get("id").asLong();

        this.updater.updateViewFromModel(this.ident.with(idNew));
    }

    private void query() {
        this.listboxLinks = new List();
        this.links = new ArrayList<>();

        {
            final Query query = new Query(
                String.format("MATCH (me:%s)-[r]->(n) WHERE ID(me) = $id RETURN r, ID(r) AS idR, n, ID(n) AS idN", this.ident.type().typename()),
                Map.of("id", this.ident.id().get()));
            final java.util.List<Record> rs;
            try (final var session = this.datastore.session()) {
                Tracer.trace(query.toString());
                rs = session.readTransaction(tx -> tx.run(query).list());
                Tracer.trace("records found: "+rs.size());
            }
            rs.forEach(r -> {
                // (this)-[r]->(n)
                final var rel = r.get("r").asRelationship();
                final var node = r.get("n").asNode();

                final var tail = this.ident.type().typename();
                final var head = node.labels().iterator().next();
                final var type = this.model.schema.of(rel.type(), tail, head);
                final var link = new DigredEntityIdent(type, rel.id());
                this.links.add(link);

                final var propsT = r.get("n").asMap(Values.ofValue());
                var nameT = propsT.get("name");
                String shead;
                if (Objects.nonNull(nameT) && !nameT.isNull() && !nameT.isEmpty()) {
                    shead = nameT.asString();
                } else {
                    shead = type.head().display(r.get("idN").asLong());
                }

                final var propsR = r.get("r").asMap(Values.ofValue());
                var nameR = propsR.get("name");
                String srel;
                if (Objects.nonNull(nameR) && !nameR.isNull() && !nameR.isEmpty()) {
                    srel = nameR.asString();
                } else {
                    srel = type.display(r.get("idR").asLong());
                }

                this.listboxLinks.add("(this) - "+srel+" -> "+shead);
            });
        }
        {
            final Query query = new Query(
                String.format("MATCH (me:%s)<-[r]-(n) WHERE ID(me) = $id RETURN r, ID(r) AS idR, n, ID(n) AS idN", this.ident.type().typename()),
                Map.of("id", this.ident.id().get()));
            final java.util.List<Record> rs;
            try (final var session = this.datastore.session()) {
                Tracer.trace(query.toString());
                rs = session.readTransaction(tx -> tx.run(query).list());
                Tracer.trace("records found: "+rs.size());
            }
            rs.forEach(r -> {
                // (n)-[r]->(this)
                final var rel = r.get("r").asRelationship();
                final var node = r.get("n").asNode();

                final var tail = node.labels().iterator().next();
                final var head = this.ident.type().typename();
                final var type = this.model.schema.of(rel.type(), tail, head);
                final var link = new DigredEntityIdent(type, rel.id());
                this.links.add(link);

                final var propsR = r.get("r").asMap(Values.ofValue());
                var nameR = propsR.get("name");
                String srel;
                if (Objects.nonNull(nameR) && !nameR.isNull() && !nameR.isEmpty()) {
                    srel = nameR.asString();
                } else {
                    srel = type.display(r.get("idR").asLong());
                }

                final var propsT = r.get("n").asMap(Values.ofValue());
                var nameT = propsT.get("name");
                String stail;
                if (Objects.nonNull(nameT) && !nameT.isNull() && !nameT.isEmpty()) {
                    stail = nameT.asString();
                } else {
                    stail = type.tail().display(r.get("idN").asLong());
                }

                this.listboxLinks.add(stail+" - "+srel+" -> (this)");
            });
        }
    }
}
