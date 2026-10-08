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
import nu.mine.mosher.graph.digred.util.*;

import java.awt.*;

public class DigredMainPanel extends Panel implements ViewUpdater {
    private final Frame owner;
    private final DigredModel model;
    private final DataStore datastore;
    private DigredVertexPanel panelVertex;
    private DigredPropsPanel panelProps;



    public static DigredMainPanel create(final Frame owner, final DigredModel model, final DataStore dataStore) {
        Tracer.trace("DigredMainPanel: create");
        final DigredMainPanel panel = new DigredMainPanel(owner, model, dataStore);
        panel.init();
        return panel;
    }

    private DigredMainPanel(final Frame owner, final DigredModel model, final DataStore dataStore) {
        this.owner = owner;
        this.model = model;
        this.datastore = dataStore;
    }

    public void init() {
        setLayout(new ThirdsLayoutManager());
        setBackground(DigredGui.debugLayout(Color.CYAN));

        this.panelProps = DigredPropsPanel.create(this.owner, this.model, this.datastore, this);
        this.panelProps.setBackground(DigredGui.debugLayout(Color.YELLOW));

        this.panelVertex = DigredVertexPanel.create(this.model, this.datastore, this.panelProps);
        this.panelVertex.setBackground(DigredGui.debugLayout(Color.MAGENTA));

        add(this.panelVertex);
        add(this.panelProps);
    }

    @Override
    public void updateViewFromModel(final DigredEntityIdent ident) {
        Tracer.trace("DigredMainPanel: updateViewFromModel");
        Tracer.trace("    ident: "+ident);
        this.panelVertex.updateViewFromModel(ident);
    }
}
