package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

import static java.awt.FlowLayout.LEADING;

public class DigredPropsForm extends Container {
    private final DataStore datastore;
    private final ViewUpdater updater;
    private final List<Component> fields = new ArrayList<>(16);
    private final List<Value> valuesOrig = new ArrayList<>(16);
    private final DigredEntityIdent ident;

    public static DigredPropsForm create(final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        Tracer.trace("DigredPropsForm: create");
        Tracer.trace("    ident: "+ident);
        final DigredPropsForm form = new DigredPropsForm(datastore, updater, ident);
        form.init();
        return form;
    }

    private DigredPropsForm(final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        this.datastore = datastore;
        this.updater = updater;
        this.ident = ident;
        if (this.ident.id().isEmpty()) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Insets getInsets() {
        return new Insets(5,5,5,5);
    }

    private void init() {
        setBackground(DigredGui.debugLayout(Color.ORANGE));

        final GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        final GridBagConstraints lay = new GridBagConstraints();
        lay.insets = new Insets(5,5,5,5);
        lay.gridx = 0;
        lay.anchor = GridBagConstraints.LINE_START;

        final var typeEntity = this.ident.type();

        final var rec = query(typeEntity, this.ident.id().get());
        final var node = rec.get("n").asEntity();

        final boolean edge = rec.containsKey("tail");

        if (edge) {
            final var p = new Container();
            p.setLayout(new FlowLayout(LEADING));
            final var e = (Edge)typeEntity;

            final var tail = rec.get("tail").asNode();
            final var vertexTail = e.tail();
            final var labelTail = new Button();
            String stail;
            final var propsT = tail.asMap(Values.ofValue());
            final var propNameT = vertexTail.propOf(DataType._DIGRED_NAME);

            Value nameT = null;
            if (propNameT.isPresent()) {
                nameT = propsT.get(propNameT.get().key());
            }
            if (Objects.nonNull(nameT) && !nameT.isNull() && !nameT.isEmpty()) {
                stail = nameT.asString();
            } else {
                stail = e.tail().display(rec.get("idTail").asLong());
            }
            labelTail.setLabel(stail);
            labelTail.addActionListener(event -> selectLink(event, vertexTail, tail.id()));
            p.add(labelTail);

            final var labelNode = new Label();
            labelNode.setAlignment(Label.CENTER);
            labelNode.setText(e.display(node.id()));
            p.add(labelNode);

            final var head = rec.get("head").asNode();
            final var vertexHead = e.head();
            final var labelHead = new Button();
            String shead;
            final var propsH = head.asMap(Values.ofValue());
            final var propNameH = vertexHead.propOf(DataType._DIGRED_NAME);

            Value nameH = null;
            if (propNameH.isPresent()) {
                nameH = propsH.get(propNameH.get().key());
            }
            if (Objects.nonNull(nameH) && !nameH.isNull() && !nameH.isEmpty()) {
                shead = nameH.asString();
            } else {
                shead = e.head().display(rec.get("idHead").asLong());
            }
            labelHead.setLabel(shead);
            labelHead.addActionListener(event -> selectLink(event, vertexHead, head.id()));
            p.add(labelHead);

            lay.weightx = 1.0D;
            lay.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(p, lay);
            lay.weightx = 0.0D;
            lay.fill = GridBagConstraints.NONE;
            add(p);
        } else {
            final var v = (Vertex)typeEntity;

            final var labelNode = new Label();
            labelNode.setAlignment(Label.CENTER);
            labelNode.setText(v.display(node.id()));
            layout.setConstraints(labelNode, lay);
            add(labelNode);
        }

        final var p = new Container();
        {
            final var layout2 = new GridBagLayout();
            p.setLayout(layout2);

            final var props = typeEntity.props();

            this.fields.clear();
            props.forEach(prop -> {
                final var lay2 = new GridBagConstraints();
                lay2.insets = new Insets(5,5,5,5);
                lay2.anchor = GridBagConstraints.LINE_START;



                final var labelProp = new Label(prop.display());

                lay2.gridx = 0;
                layout2.setConstraints(labelProp, lay2);
                p.add(labelProp);



                final var val = node.get(prop.key());
                this.valuesOrig.add(val);

                final Component cmp = DigredDataConverter.componentOfValue(val, prop);
                this.fields.add(cmp);

                lay2.gridx = 1;
                lay2.weightx = 1.0D;
                lay2.fill = GridBagConstraints.HORIZONTAL;
                layout2.setConstraints(cmp, lay2);
                p.add(cmp);
            });
        }
        lay.weightx = 1.0D;
        lay.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(p, lay);
        lay.weightx = 0.0D;
        lay.fill = GridBagConstraints.NONE;
        add(p);

        final var b = new Container();
        b.setLayout(new FlowLayout());
        final var buttonDelete = new Button("Delete");
        buttonDelete.addActionListener(this::pressedDelete);
        b.add(buttonDelete);
        final var buttonCancel = new Button("Cancel");
        buttonCancel.addActionListener(this::pressedCancel);
        b.add(buttonCancel);
        final var buttonSave = new Button("Save");
        buttonSave.addActionListener(this::pressedSave);
        b.add(buttonSave);
        layout.setConstraints(b, lay);
        add(b);
    }

    private void selectLink(final ActionEvent e, final Vertex vertex, final long id) {
        Tracer.trace("SELECT LINK: "+e.paramString());
        this.updater.updateViewFromModel(new DigredEntityIdent(vertex, id));
    }

    private Record query(final Entity typeEntity, final Long idEntity) {
        final Query query;
        if (typeEntity.vertex()) {
            // TODO use pk instead (but still need to handle case where pk is not defined in schema)
            query = new Query(String.format("MATCH (n:%s) WHERE ID(n) = $id RETURN n",
                typeEntity.typename()),
                Map.of("id", idEntity));
        } else {
            query = new Query(String.format(
                "MATCH (tail)-[n:%s]->(head) WHERE ID(n) = $id " +
                "RETURN n, tail, ID(tail) AS idTail, head, ID(head) AS idHead",
                typeEntity.typename()),
                Map.of("id", idEntity));
        }

        final Record rs;
        try (final var session = this.datastore.session()) {
            Tracer.trace(query.toString());
            rs = session.readTransaction(tx -> tx.run(query).single());
            Tracer.trace("records found: "+rs.size());
        }

        return rs;
    }

    private void pressedDelete(final ActionEvent e) {
        Tracer.trace("DELETE: "+e.paramString());
        if (DigredOkCancel.run(null, "WARNING! This will permanently delete this entity.")) {
            final var entity = this.ident.type();
            final var query = new Query(
                String.format(
                    entity.vertex()
                    ? "MATCH (n:%s) WHERE ID(n) = $id DETACH DELETE n"
                    : "MATCH ()-[r:%s]-() WHERE ID(r) = $id DELETE r",
                    entity.typename()),
                Map.of(
                    "id", this.ident.id().get()));

            try (final var session = datastore.session()) {
                Tracer.trace(query.toString());
                session.writeTransaction(tx -> tx.run(query).consume());
            }

            this.updater.updateViewFromModel(new DigredEntityIdent(this.ident.type()/* TODO modified date? */));
        }
    }

    private void pressedCancel(final ActionEvent e) {
        Tracer.trace("CANCEL: "+e.paramString());
        final var props = this.ident.type().props();
        if (this.fields.size() != props.size() || this.valuesOrig.size() != props.size()) {
            throw new IllegalStateException("error in list of fields");
        }
        for (int i = 0; i < props.size(); ++i) {
            final var prop = props.get(i);
            final var valueOrig = this.valuesOrig.get(i);
            final var cmp = this.fields.get(i);
            DigredDataConverter.setComponentValue(valueOrig, prop, cmp);
        }
    }

    private void pressedSave(final ActionEvent e) {
        Tracer.trace("SAVE: "+e.paramString());
        final var entity = this.ident.type();

        final var props = entity.props();
        if (this.fields.size() != props.size() || this.valuesOrig.size() != props.size()) {
            throw new IllegalStateException("error in list of fields");
        }

        final var cyRemoves = new ArrayList<String>();
        final var params = new HashMap<String, Object>();
        for (int i = 0; i < props.size(); ++i) {
            final var prop = props.get(i);
            if (!DigredDataConverter.readonly(prop)) {
                final var valOrig = this.valuesOrig.get(i);
                final var valNew = DigredDataConverter.valueOfComponent(this.fields.get(i), prop);
                if (!valNew.equals(valOrig)) {
                    Tracer.trace("detected change: " + prop.key() + ": " + valOrig + " --> " + valNew);
                    if (valNew.isNull()) {
                        cyRemoves.add("n." + prop.key());
                    } else {
                        params.put(prop.key(), valNew);
                    }
                }
            }
        }

        if (!cyRemoves.isEmpty() || !params.isEmpty()) {
            final var propVersion = entity.propOf(DataType._DIGRED_VERSION);
            final var propModified = entity.propOf(DataType._DIGRED_MODIFIED);
            final var query = new Query(
                String.format(
                    entity.vertex()
                    ? "MATCH (n:%s) WHERE ID(n) = $id SET n += $map %s %s %s"
                    : "MATCH ()-[n:%s]-() WHERE ID(n) = $id SET n += $map %s %s %s",
                    entity.typename(),
                    propVersion.isPresent() ? ", n."+propVersion.get().key()+" = n."+propVersion.get().key()+"+1" : "",
                    propModified.isPresent() ? ", n."+propModified.get().key()+" = datetime.realtime()" : "",
                    cyRemoves.isEmpty() ? "" : ("REMOVE " + String.join(",", cyRemoves))),
                Map.of(
                    "id", this.ident.id().get(),
                    "map", params));

            try (final var session = datastore.session()) {
                Tracer.trace(query.toString());
                session.writeTransaction(tx -> tx.run(query).consume());
            }
        }

        this.updater.updateViewFromModel(this.ident);
    }
}
