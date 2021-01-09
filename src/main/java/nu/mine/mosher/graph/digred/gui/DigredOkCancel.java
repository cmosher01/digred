package nu.mine.mosher.graph.digred.gui;

import java.awt.*;
import java.awt.event.ActionEvent;

public class DigredOkCancel extends Dialog {
    private final String message;
    private boolean ok;

    public static boolean run(final Frame owner, final String message) {
        final var dialog = new DigredOkCancel(owner, message);
        dialog.init();
        dialog.setVisible(true);
        return dialog.ok;
    }

    private DigredOkCancel(final Frame owner, final String message) {
        super(owner, true);
        this.message = message;
    }

    private void init() {
        setSize(690, 180);
        setLocation(180, 180);

        final var labels = new Panel();
        labels.setLayout(new FlowLayout(FlowLayout.LEADING, 20, 20));
        labels.add(new Label(this.message));
        add(labels);

        final var buttons = new Panel();
        buttons.setLayout(new FlowLayout(FlowLayout.TRAILING, 20, 20));
        final var cancel = new Button("Cancel");
        cancel.addActionListener(this::pressedCancel);
        buttons.add(cancel);
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
