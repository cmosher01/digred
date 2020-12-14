package nu.mine.mosher.graph.digred.gui;

import org.slf4j.*;

import java.awt.*;

public class DigredMenuBar extends MenuBar {
    private static final Logger LOG = LoggerFactory.getLogger(DigredMenuBar.class);

    public void init(final DigredFrame frame) {
        final Menu menuFile = new Menu("File");

        final MenuItem itemOpen = frame.initFileOpenMenuItem();
        menuFile.add(itemOpen);
        final MenuItem itemClose = frame.initFileCloseMenuItem();
        menuFile.add(itemClose);

        menuFile.addSeparator();

        final MenuItem itemQuit = initFileQuitMenuItem(frame);
        menuFile.add(itemQuit);

//        TODO setHelpMenu();

        add(menuFile);
    }

    private static MenuItem initFileQuitMenuItem(final DigredFrame frame) {
        final MenuItem itemQuit = new MenuItem("Exit");
        itemQuit.addActionListener(e -> fileExit(frame));
        return itemQuit;
    }

    private static void fileExit(final DigredFrame frame) {
        LOG.info("File/Exit menu item chosen.");
        frame.quitIfSafe();
    }
}
