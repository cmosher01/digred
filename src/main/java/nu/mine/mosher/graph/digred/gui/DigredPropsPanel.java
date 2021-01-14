package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.slf4j.*;

import java.awt.*;
import java.util.Objects;

public class DigredPropsPanel extends Panel implements ViewUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DigredPropsPanel.class);

    private final Frame owner;
    private final DigredModel model;
    private final DataStore datastore;
    private final ViewUpdater updater;
    private DigredPropsForm form;
    private DigredLinksPanel links;
    private Container filler;

    public static DigredPropsPanel create(final Frame owner, final DigredModel model, final DataStore datastore, final ViewUpdater updater) {
        Tracer.trace("DigredPropsPanel: create");
        final var panel = new DigredPropsPanel(owner, model, datastore, updater);
        panel.init();
        return panel;
    }

    private DigredPropsPanel(final Frame owner, final DigredModel model, final DataStore datastore, final ViewUpdater updater) {
        this.owner = owner;
        this.model = model;
        this.datastore = datastore;
        this.updater = updater;
    }

    public void init() {
        setLayout(null);
        setBackground(DigredGui.debugLayout(Color.BLUE));
    }

    @Override
    public void updateViewFromModel(final DigredEntityIdent ident) {
        Tracer.trace("DigredPropsPanel: updateViewFromModel");
        Tracer.trace("    ident: "+ident);
        if (Objects.nonNull(this.form)) {
            remove(this.form);
            this.form = null;
        }
        if (Objects.nonNull(this.links)) {
            remove(this.links);
            this.links = null;
        }
        if (Objects.nonNull(this.filler)) {
            remove(this.filler);
            this.filler = null;
        }
        if (ident.id().isPresent()) {
            final var layoutManager = new GridBagLayout();
            setLayout(layoutManager);
            final var cns = new GridBagConstraints();
            cns.gridx = 0;
            cns.anchor = GridBagConstraints.NORTHWEST;

            this.form = DigredPropsForm.create(this.datastore, this.model, this.updater, ident);
            cns.weightx = 1.0D;
            cns.fill = GridBagConstraints.HORIZONTAL;
            layoutManager.setConstraints(this.form, cns);
            add(this.form);

            if (ident.type().vertex()) {
                this.links = DigredLinksPanel.create(this.owner, this.model, this.datastore, this.updater, ident);
                layoutManager.setConstraints(this.links, cns);
                add(this.links);
            }

            cns.weightx = 1.0D;
            cns.weighty = 1.0D;
            cns.fill = GridBagConstraints.BOTH;
            this.filler = new Container();
            layoutManager.setConstraints(this.filler, cns);
            add(this.filler);
        }
        validate();
    }
}
