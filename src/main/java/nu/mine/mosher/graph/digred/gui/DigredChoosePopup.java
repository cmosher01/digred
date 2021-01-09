package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.Entity;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.awt.List;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static java.awt.event.KeyEvent.*;
import static nu.mine.mosher.graph.digred.gui.DigredVertexPanel.resultDisplayNameOf;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DigredChoosePopup extends Dialog {
    public static Optional<Long> run(final Frame owner, final DataStore dataStore, final Entity vertexChoose) {
        final var popup = new DigredChoosePopup(owner, dataStore, vertexChoose);
        popup.init();
        popup.queryEntities();
        popup.setVisible(true);
        return popup.id;
    }

    private final DataStore datastore;
    private final Entity vertexChoose;

    private List listboxResults;
    private java.util.List<Record> listResults;

    private Optional<Long> id = Optional.empty();

    private DigredChoosePopup(final Frame owner, final DataStore dataStore, final Entity vertexChoose) {
        super(owner, vertexChoose.display(), true);
        this.datastore = dataStore;
        this.vertexChoose = vertexChoose;
    }

    private void init() {
        final var esc = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == VK_ESCAPE) {
                    done(null);
                }
            }
        };

        final var ret = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == VK_ENTER) {
                    choseVertex(null);
                }
            }
        };

        setLayout(new BorderLayout());
        setSize(640, 1080);

        this.listboxResults = new List();
        this.listboxResults.addActionListener(this::choseVertex);
        this.listboxResults.addKeyListener(esc);
        add(this.listboxResults);



        final var buttons = new Container();
        buttons.setLayout(new FlowLayout());

        final var buttonCancel = new Button("Cancel");
        buttonCancel.addActionListener(this::done);
        buttonCancel.addKeyListener(esc);
        buttons.add(buttonCancel);

        final var buttonOK = new Button("OK");
        buttonOK.addActionListener(this::choseVertex);
        buttonOK.addKeyListener(esc);
        buttonOK.addKeyListener(ret);
        buttons.add(buttonOK);

        add(buttons, "South");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent ev) {
                done(null);
            }
        });

        relocateWindow(this);
    }

    private void choseVertex(final ActionEvent e) {
        final int i = this.listboxResults.getSelectedIndex();
        if (0 <= i) {
            final var rec = this.listResults.get(i);
            this.id = Optional.of(rec.get("id").asLong());
        }
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
            this.listboxResults.select(0);
            EventQueue.invokeLater(() -> this.listboxResults.requestFocus());
        }
    }

    private void relocateWindow(final Window window) {
        window.setSize(getBounds().width-100, getBounds().height-100);
        window.setLocationRelativeTo(this);
    }
}
