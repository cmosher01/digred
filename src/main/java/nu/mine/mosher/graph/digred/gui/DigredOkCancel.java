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

import java.awt.*;
import java.awt.event.ActionEvent;

public class DigredOkCancel extends Dialog {
    private final String message;
    private final boolean withCancel;
    private boolean ok;

    public static boolean run(final Frame owner, final String message, final boolean withCancel) {
        final var dialog = new DigredOkCancel(owner, message, withCancel);
        dialog.init();
        dialog.setVisible(true);
        return dialog.ok;
    }

    private DigredOkCancel(final Frame owner, final String message, final boolean withCancel) {
        super(owner, true);
        this.message = message;
        this.withCancel = withCancel;
    }

    private void init() {
        setSize(690, 300);
        setLocation(180, 180);

        final var labels = new Panel();
        labels.setLayout(new FlowLayout(FlowLayout.LEADING, 20, 20));
        final var label = new TextArea(this.message);
        label.setEditable(false);
        labels.add(label);
        add(labels);

        final var buttons = new Panel();
        buttons.setLayout(new FlowLayout(FlowLayout.TRAILING, 20, 20));
        if (withCancel) {
            final var cancel = new Button("Cancel");
            cancel.addActionListener(this::pressedCancel);
            buttons.add(cancel);
        }
        final var ok = new Button("OK");
        ok.addActionListener(this::pressedOK);
        EventQueue.invokeLater(ok::requestFocus);
        buttons.add(ok);
        add(buttons, "South");
    }

    private void pressedOK(final ActionEvent e) {
        this.ok = true;
        pressedCancel(e);
    }

    private void pressedCancel(final ActionEvent e) {
        setVisible(false);
        dispose();
    }
}
