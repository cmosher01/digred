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
