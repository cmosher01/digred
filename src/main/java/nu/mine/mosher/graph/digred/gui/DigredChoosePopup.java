package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.Entity;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Optional;

import static nu.mine.mosher.graph.digred.gui.DigredVertexPanel.resultDisplayNameOf;

public class DigredChoosePopup extends Dialog {
    public static Optional<Long> run(final DigredModel model, final DataStore dataStore, final Entity vertexChoose) {
        // TODO pass main frame as parent to this dialog
        final var popup = new DigredChoosePopup(null, model, dataStore, vertexChoose);
        popup.init();
        popup.queryEntities();
        popup.setVisible(true);
        return popup.id;
    }

    private final DigredModel model;
    private final DataStore datastore;
    private final Entity vertexChoose;

    private List listboxResults;
    private java.util.List<Record> listResults;
    private Button buttonOK;
    private Button buttonCancel;

    private Optional<Long> id = Optional.empty();

    private DigredChoosePopup(final Frame owner, final DigredModel model, final DataStore dataStore, final Entity vertexChoose) {
        super(owner, vertexChoose.display(), true);
        this.model = model;
        this.datastore = dataStore;
        this.vertexChoose = vertexChoose;
    }

    private void init() {
        setLayout(new BorderLayout());
        setSize(640, 1080);



        this.listboxResults = new List();
        this.listboxResults.addActionListener(this::choseVertex);
        add(this.listboxResults);



        final var buttons = new Container();
        buttons.setLayout(new FlowLayout());

        this.buttonCancel = new Button("Cancel");
        this.buttonCancel.addActionListener(this::done);
        buttons.add(this.buttonCancel);

        this.buttonOK = new Button("OK");
        this.buttonOK.addActionListener(this::choseVertex);
        buttons.add(this.buttonOK);

        add(buttons, "South");

        // TODO capture window-close event
        // TODO: disable OK button if nothing selected in list
    }

    private void choseVertex(final ActionEvent e) {
        final var rec = this.listResults.get(this.listboxResults.getSelectedIndex());
        this.id = Optional.of(rec.get("id").asLong());
        done(e);
    }

    private void done(final ActionEvent e) {
        setVisible(false);
        dispose();
    }

    private void queryEntities() {
        final Entity vertex = this.vertexChoose;
        final Query query;
//        if (this.search.isBlank()) {
            query = new Query(String.format("MATCH (n:%s) " +
                    "RETURN n {.pk, .modified, .name }, ID(n) AS id " +
                    "ORDER BY n.modified DESC "+
                    "LIMIT 100",
                vertex.typename()));
//        } else {
            // TODO google-style full-text search
//            query = new Query("TODO");
//        }

        final java.util.List<Record> rs;
        try (final var session = datastore.session()) {
            rs = session.readTransaction(tx -> tx.run(query).list());
        }

        this.listResults = new ArrayList<>();
        this.listboxResults.removeAll();
        rs.forEach(r -> {
            this.listResults.add(r);
            this.listboxResults.add(resultDisplayNameOf(vertex, r));
        });

        if (!this.listResults.isEmpty()) {
            EventQueue.invokeLater(() -> this.listboxResults.requestFocus());
        }
    }
}
