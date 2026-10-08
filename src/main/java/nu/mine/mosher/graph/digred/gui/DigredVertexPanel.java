/*
 *     Copyright 2020, 2021, 2026, Christopher Alan Mosher, New York, New York, USA, <cmosher01@gmail.com>.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import java.util.ArrayList;

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
        this.model.schema.e().forEach(v -> this.choiceVertex.add(DigredDataConverter.displaySimpleRelType(v)));
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
        final var id = record.get("n").asEntity().elementId();
        this.ident = this.ident.with(id);
        this.updater.updateViewFromModel(ident);
    }

    // User command: pressed "New" button to create a new entity
    private void pressedNew(final ActionEvent e) {
        Tracer.trace("NEW: "+e.paramString());
        final var entity = this.ident.type();
        if (entity.vertex()) {
            final var cyProps = DigredDataConverter.digredCypherProps(entity);

            final var query = new Query(String.format(
                "CREATE (n:%s { %s }) RETURN elementId(n)",
                entity.typename(),
                String.join(",", cyProps)));

            final Record rec;
            try (final var session = datastore.session()) {
                Tracer.trace(query.toString());
                rec = session.executeWrite(tx -> tx.run(query).single());
            }

            updateViewFromModel(this.ident.with(rec.get("pk").asString()));
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
            final var propMod = vertex.propOf(DataType._DIGRED_MODIFIED);
            query = new Query(
                    String.format(
                        "MATCH (n:%s) " +
                        "RETURN n, elementId(n) AS id " +
                        (propMod.map(prop -> "ORDER BY n." + prop.key() + " DESC ").orElse("")) +
                        "LIMIT 100",
                    vertex.typename()));
//            } else {
                // TODO google-style full-text search
//                query = new Query("TODO");
//            }
        } else {
            final Edge edge = (Edge)entity;
            final var propMod = edge.propOf(DataType._DIGRED_MODIFIED);
            query = new Query(
                String.format(
                    "MATCH (tail:%s)-[n:%s]->(head:%s) "+
                    "RETURN tail, n, head "+
                    (propMod.map(prop -> "ORDER BY n." + prop.key() + " DESC ").orElse("")) +
                    "LIMIT 100",
                edge.tail().typename(),
                edge.typename(),
                edge.head().typename()));
        }

        final java.util.List<Record> rs;
        try (final var session = datastore.session()) {
            Tracer.trace(query.toString());
            rs = session.executeRead(tx -> tx.run(query).list());
            Tracer.trace("records found: "+rs.size());
        }

        this.listResults = new ArrayList<>();
        this.listboxResults.removeAll();
        String idFirst = "";
        int preselect = -1;
        for (final var r : rs) {
            this.listResults.add(r);

            final String name;
            if (entity.vertex()) {
                name = DigredDataConverter.displayNode(r.get("n").asNode(), this.model, true);
            } else {
                name = DigredDataConverter.displayRel(r.get("n").asRelationship(), r.get("tail").asNode(), r.get("head").asNode(), this.model);
            }
            this.listboxResults.add(name);

            final var id = r.get("n").asEntity().elementId();
            if (idFirst.isBlank()) {
                idFirst = id;
            }
            if (this.ident.id().isPresent()) {
                if (id.equals(this.ident.id().get())) {
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
}
