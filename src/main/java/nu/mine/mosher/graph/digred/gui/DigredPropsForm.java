package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.value.NullValue;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

import static java.awt.FlowLayout.LEADING;

public class DigredPropsForm extends Container {
    private final DigredModel model;
    private final DataStore datastore;
    private final ViewUpdater updater;
    private final List<Component> fields = new ArrayList<>(16);
    private List<Value> valuesOrig = new ArrayList<>(16);
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonDelete;
    private final DigredEntityIdent ident;

    public static DigredPropsForm create(final DigredModel model, final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        Tracer.trace("DigredPropsForm: create");
        Tracer.trace("    ident: "+ident);
        final DigredPropsForm form = new DigredPropsForm(model, datastore, updater, ident);
        form.init();
        return form;
    }

    private DigredPropsForm(final DigredModel model, final DataStore datastore, final ViewUpdater updater, final DigredEntityIdent ident) {
        this.model = model;
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
            final var vertexTail = this.model.schema.of(tail.labels().iterator().next());
            final var labelTail = new Button();
            String stail;
            final var propsT = tail.asMap(Values.ofValue());
            var nameT = propsT.get("name");
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
            final var vertexHead = this.model.schema.of(head.labels().iterator().next());
            final var labelHead = new Button();
            String shead;
            final var propsH = head.asMap(Values.ofValue());
            var nameH = propsH.get("name");
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



                final var value = node.get(prop.key());
                this.valuesOrig.add(value);

                final Component ctrlProp;
                switch (prop.type()) {
                    case TEXT -> {
                        final TextArea t = new TextArea(displayValueOf(value));
                        t.setEditable(!readonly(prop) && canConvert(value));
                        ctrlProp = t;
                    }
                    case BOOLEAN -> {
                        final Checkbox c = new Checkbox(prop.key(), value.isNull() ? false : value.asBoolean());
                        c.setEnabled(!readonly(prop) && canConvert(value));
                        ctrlProp = c;
                    }
                    default -> {
                        final TextField t = new TextField(displayValueOf(value));
                        t.setEditable(!readonly(prop) && canConvert(value));
                        ctrlProp = t;
                    }
                }
                this.fields.add(ctrlProp);

                lay2.gridx = 1;
                lay2.weightx = 1.0D;
                lay2.fill = GridBagConstraints.HORIZONTAL;
                layout2.setConstraints(ctrlProp, lay2);
                p.add(ctrlProp);
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
        this.buttonDelete = new Button("Delete");
        this.buttonDelete.addActionListener(this::pressedDelete);
        b.add(this.buttonDelete);
        this.buttonCancel = new Button("Cancel");
        this.buttonCancel.addActionListener(this::pressedCancel);
        b.add(this.buttonCancel);
        this.buttonSave = new Button("Save");
        this.buttonSave.addActionListener(this::pressedSave);
        b.add(this.buttonSave);
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
            switch (prop.type()) {
                case BOOLEAN -> ((Checkbox)cmp).setState(valueOrig.isNull() ? false : valueOrig.asBoolean());
                default -> ((TextComponent)cmp).setText(displayValueOf(valueOrig));
            }
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
        boolean hasVersion = false;
        boolean hasModified = false;
        for (int i = 0; i < props.size(); ++i) {
            final var prop = props.get(i);
            if (prop.type() == DataType._DIGRED_VERSION) {
                hasVersion = true;
            } else if (prop.type() == DataType._DIGRED_MODIFIED) {
                hasModified = true;
            } else if (prop.type() == DataType._DIGRED_CREATED) {
                // ignore
            } else if (prop.type() == DataType._DIGRED_PK) {
                // ignore
            } else {
                final var valueOrig = this.valuesOrig.get(i);
                final var cmp = this.fields.get(i);
                final Value valueNew = switch (prop.type()) {
                    // TODO handle invalid format
                    case INTEGER -> Values.value(Long.parseLong(((TextComponent)cmp).getText(),10));
                    case FLOAT -> Values.value(Double.parseDouble(((TextComponent)cmp).getText()));
                    case BOOLEAN -> Values.value(((Checkbox)cmp).getState());
                    // TODO other types
                    default -> {
                        final var t = ((TextComponent)cmp).getText();
                        yield t.isEmpty() ? NullValue.NULL : Values.value(t);
                    }
                };
                if (!valueNew.equals(valueOrig)) {
                    Tracer.trace("detected change: " + prop.key() + ": " + valueOrig + " --> " + valueNew);
                    if (valueNew.isNull()) {
                        cyRemoves.add("n." + prop.key());
                    } else {
                        params.put(prop.key(), valueNew);
                    }
                }
            }
        }

        if (!cyRemoves.isEmpty() || !params.isEmpty()) {
            final var query = new Query(
                String.format(
                    entity.vertex()
                    ? "MATCH (n:%s) WHERE ID(n) = $id SET n += $map %s %s %s"
                    : "MATCH ()-[n:%s]-() WHERE ID(n) = $id SET n += $map %s %s %s",
                    entity.typename(),
                    hasVersion ? ", n.version = n.version+1" : "",
                    hasModified ? ", n.modified = datetime.realtime()" : "",
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

    private static boolean canConvert(final Value value) {
        return
            TypeConstructor.STRING.covers(value) ||
            TypeConstructor.INTEGER.covers(value) ||
            TypeConstructor.FLOAT.covers(value) ||
            TypeConstructor.DATE_TIME.covers(value) ||
            TypeConstructor.BOOLEAN.covers(value) ||
            TypeConstructor.NULL.covers(value);
        /*
            TODO: handle remaining datatypes:
            DATE
            TIME
            LOCAL_TIME
            LOCAL_DATE_TIME
            DURATION
            POINT
         */
    }

    public static String displayValueOf(final Value value) {
        if (Objects.isNull(value)) {
            return "";
        }
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
        // In Neo4j, property values cannot be NULL; rather, the property simply wouldn't exist.
        // However, a STRING property can exist and be empty.
        // Currently we treat empty string as NULL
        // TODO how to distinguish between NULL and empty string?
        if (TypeConstructor.NULL.covers(value)) {
            return "";
        }
        return "[cannot convert value of type "+value.type().name()+" for display]";
    }

    private static final Set<DataType> setReadOnly = Set.of(
        DataType._DIGRED_PK,
        DataType._DIGRED_CREATED,
        DataType._DIGRED_MODIFIED,
        DataType._DIGRED_VERSION
    );

    private static boolean readonly(final Prop prop) {
        return setReadOnly.contains(prop.type());
    }
}
