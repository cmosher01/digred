package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.Digred;
import nu.mine.mosher.graph.digred.datastore.DataStore;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;

import static java.awt.event.KeyEvent.*;

public class DigredDatabasePopup extends Dialog {
    public static void run(final Frame owner, final DataStore dataStore) {
        final var popup = new DigredDatabasePopup(owner, dataStore);
        popup.init();
        popup.setVisible(true);
    }

    private final DataStore datastore;
    private TextField textUrlDatabase;
    private TextField textUsername;
    private TextField textPassword;

    private DigredDatabasePopup(final Frame owner, final DataStore dataStore) {
        super(owner, "Connect to Database", true);
        this.datastore = dataStore;
    }

    private void init() {
        setSize(690, 400);
        relocateWindow();



        final var ret = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == VK_ENTER) {
                    pressedOK(null);
                }
            }
        };

        final var esc = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == VK_ESCAPE) {
                    pressedCancel(null);
                }
            }
        };



        final var fields = new Panel();
        final var layout = new GridBagLayout();
        fields.setLayout(layout);
        final var lay = new GridBagConstraints();
        lay.insets = new Insets(5,20,5,20);
        lay.anchor = GridBagConstraints.LINE_START;

        lay.gridy = 0;
        final var labelDatabase = new Label("Database URL:");
        lay.gridx = 0;
        layout.setConstraints(labelDatabase, lay);
        fields.add(labelDatabase);

        lay.gridy = 1;
        final var labelUsername = new Label("Username:");
        lay.gridx = 0;
        layout.setConstraints(labelUsername, lay);
        fields.add(labelUsername);

        lay.gridy = 2;
        final var labelPassword = new Label("Password:");
        lay.gridx = 0;
        layout.setConstraints(labelPassword, lay);
        fields.add(labelPassword);

        lay.weightx = 1.0D;
        lay.fill = GridBagConstraints.HORIZONTAL;

        lay.gridy = 0;
        this.textUrlDatabase = new TextField(Digred.prefs().get("databaseUrl", DataStore.uriDefault().toASCIIString()));
        this.textUrlDatabase.addKeyListener(ret);
        this.textUrlDatabase.addKeyListener(esc);
        lay.gridx = 1;
        layout.setConstraints(this.textUrlDatabase, lay);
        fields.add(this.textUrlDatabase);
        add(fields);

        lay.gridy = 1;
        this.textUsername = new TextField(Digred.prefs().get("databaseUsername", DataStore.usernameDefault()));
        this.textUsername.addKeyListener(ret);
        this.textUsername.addKeyListener(esc);
        lay.gridx = 1;
        layout.setConstraints(this.textUsername, lay);
        fields.add(this.textUsername);

        lay.gridy = 2;
        this.textPassword = new TextField(Digred.prefs().get("databasePassword", DataStore.passwordDefault()));
        this.textPassword.setEchoChar('\u00B7');
        this.textPassword.addKeyListener(ret);
        this.textPassword.addKeyListener(esc);
        lay.gridx = 1;
        layout.setConstraints(this.textPassword, lay);
        fields.add(this.textPassword);



        final var buttons = new Panel();
        buttons.setLayout(new FlowLayout(FlowLayout.TRAILING, 20, 20));
        final var cancel = new Button("Cancel");
        cancel.addKeyListener(esc);
        cancel.addActionListener(this::pressedCancel);
        buttons.add(cancel);
        final var ok = new Button("OK");
        ok.addKeyListener(ret);
        ok.addKeyListener(esc);
        ok.addActionListener(this::pressedOK);
        EventQueue.invokeLater(ok::requestFocus);
        buttons.add(ok);
        add(buttons, "South");
    }

    private void pressedOK(final ActionEvent e) {
        this.datastore.connect(URI.create(this.textUrlDatabase.getText()), this.textUsername.getText(), this.textPassword.getText());
        pressedCancel(e);
    }

    private void pressedCancel(final ActionEvent e) {
        Digred.prefs().put("databaseUrl", this.textUrlDatabase.getText());
        Digred.prefs().put("databaseUsername", this.textUsername.getText());
        // do not save password
        setVisible(false);
        dispose();
    }

    private void relocateWindow() {
        setSize(getBounds().width-100, getBounds().height-100);
        setLocationRelativeTo(this);
    }
}
