package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.Digred;
import nu.mine.mosher.graph.digred.datastore.DataStore;
import nu.mine.mosher.graph.digred.schema.*;
import nu.mine.mosher.graph.digred.util.Tracer;
import org.antlr.v4.runtime.*;
import org.slf4j.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.event.KeyEvent.*;

class DigredFrame extends Frame {
    private static final Logger LOG = LoggerFactory.getLogger(DigredFrame.class);
    private static final Component CENTER_ON_SCREEN = null;

    private final AtomicBoolean shutdownInProgress = new AtomicBoolean();
    private final DataStore datastore = new DataStore();
    private DigredMainPanel panelMain;
    private MenuItem itemOpen;
    private MenuItem itemClose;



    public static DigredFrame create() {
        Tracer.trace("DigredFrame: create");
        final DigredFrame frame = new DigredFrame();
        frame.init();
        return frame;
    }

    private DigredFrame() {
        super("DiGrEd");
    }

    private void init() {
        setSize(1920,1080);
        setLocationRelativeTo(CENTER_ON_SCREEN);

        // TODO add menu items to allow user to connect and disconnect to the database
        this.datastore.connect(DataStore.NEO, "neo4j", "neo4j"/*"admin"*/);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Application shutdown detected; running shutdown hook...");
            quitIfSafe();
            LOG.info("Application shutdown hook processing is complete.");
        }));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent ev) {
                LOG.info("Received a request to close the main window.");
                quitIfSafe();
            }
        });

        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler((e, r) -> {
                    LOG.info("Received a request from the desktop environment to quit the application.");
                    r.cancelQuit();
                    quitIfSafe();
                });
            }
        }



        setVisible(true);
    }

    public boolean quitIfSafe() {
        if (this.shutdownInProgress.get()) {
            LOG.info("Application shutdown is already in progress.");
            return true;
        }

        final boolean safe = isSafeToExit();
        if (safe) {
            quitApp();
        }
        return safe;
    }

    public boolean isSafeToExit() {
        boolean safe = false;
        // TODO dirty check
//        if (???.dirty()) {
//            final int response = JOptionPane.showConfirmDialog(
//                frame,
//                "Your unsaved changes will be DISCARDED.",
//                "DISCARD CHANGES",
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.WARNING_MESSAGE);
//            if (response == JOptionPane.OK_OPTION) {
//                LOG.warn("User confirmed discarding changes:");
//                (dump changes to log file, just in case)
//                safe = true;
//            }
//        } else {
            safe = true;
//        }
        if (safe) {
            LOG.info("Determined that it is safe to shut down the application.");
        } else {
            LOG.info("Determined that it is NOT SAFE to shut down the application.");
        }
        return safe;
    }

    private void quitApp() {
        LOG.info("Beginning an orderly shutdown of the application...");
        this.shutdownInProgress.set(true);
        this.datastore.disconnect();
        EventQueue.invokeLater(this::dispose);
    }

    public MenuItem initFileOpenMenuItem() {
        this.itemOpen = new MenuItem("Open...");
        this.itemOpen.setShortcut(new MenuShortcut(VK_O));
        this.itemOpen.addActionListener(this::fileOpen);
        return this.itemOpen;
    }

    // User command: File/Open
    private void fileOpen(final ActionEvent e) {
        try {
            tryFileOpen();
        } catch (final Exception ex) {
            // TODO display nice error to user
            LOG.info("Error occurred while tyring to open schema file", ex);
        }
    }

    private void tryFileOpen() throws IOException {
        final File file = askOpenFile();
        if (Objects.nonNull(file)) {
            Tracer.trace("OPEN FILE: "+file);

            fileClose(null);

            final var in = CharStreams.fromStream(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.UTF_8);

            final var lexer = new DigredLexer(in);
            final var tokens = new CommonTokenStream(lexer);
            final var parser = new DigredParser(tokens);
            final var ctx = parser.schema();

            DigraphSchema schema = ctx.scm;
            logSchema(schema);
            schema = schema.withCommonApplied();
//            logSchema(schema);

            this.panelMain = DigredMainPanel.create(this, new DigredModel(schema), this.datastore);
            add(this.panelMain);
            updateFileMenu();



            // get the first type in the list, that's the one we will display initially
            // and, we don't specify an ID of an entity to find and pre-select,
            // so it will default to the latest entities
            final var ident = new DigredEntityIdent(schema.e().get(0));
            this.panelMain.updateViewFromModel(ident);



            validate();
        }
    }

    private File askOpenFile() {
        final FileDialog dialog = new FileDialog(this, "Choose a schema file");
        dialog.setDirectory(dir().getAbsolutePath());
        dialog.setFile("*.digr");
        relocateWindow(dialog);
        dialog.setVisible(true);
        dir(new File(dialog.getDirectory()));

        final File[] files = dialog.getFiles();
        if (Objects.isNull(files) || files.length <= 0) {
            return null;
        }

        return files[0];
    }

    private static File dir() {
        return new File(Digred.prefs().get("dir", "./"));
    }

    private static void dir(final File dir) {
        Digred.prefs().put("dir", dir.getAbsolutePath());
    }

    private void relocateWindow(final Window window) {
        window.setSize(getBounds().width-100, getBounds().height-100);
        window.setLocationRelativeTo(this);
    }

    public MenuItem initFileCloseMenuItem() {
        this.itemClose = new MenuItem("Close");
        this.itemClose.setShortcut(new MenuShortcut(VK_W));
        this.itemClose.addActionListener(this::fileClose);
        return this.itemClose;
    }

    // User command: File/Close
    private void fileClose(final ActionEvent e) {
        if (Objects.nonNull(this.panelMain)) {
            remove(this.panelMain);
            this.panelMain = null;
            updateFileMenu();
        }
    }

    public void updateFileMenu() {
        final var loaded = Objects.nonNull(this.panelMain);

        this.itemClose.setEnabled(loaded);
        this.itemOpen.setEnabled(!loaded);
    }





    private static void logSchema(final DigraphSchema schema) {
        LOG.info("loading schema model:");
        if (LOG.isInfoEnabled()) {
            final StringWriter out = new StringWriter(1024);
            final BufferedWriter buf = new BufferedWriter(out);
            final PrintWriter pw = new PrintWriter(buf);
            schema.decompile(pw);
            pw.flush();
            final StringReader in = new StringReader(out.toString());
            final BufferedReader buf2 = new BufferedReader(in);
            buf2.lines().forEach(LOG::info);
        }
    }
}
