package nu.mine.mosher.graph.digred.gui;

import org.slf4j.*;

import java.awt.*;
import java.awt.event.*;

public class DigredMenuBar extends MenuBar {
    private static final Logger LOG = LoggerFactory.getLogger(DigredMenuBar.class);

    private final DigredFrame frame;



    public static DigredMenuBar create(final DigredFrame frame) {
        final DigredMenuBar menuBar = new DigredMenuBar(frame);
        menuBar.init();
        return menuBar;
    }

    private DigredMenuBar(final DigredFrame frame) {
        this.frame = frame;
    }

    private void init() {
        final Menu menuFile = new Menu("File");

        final MenuItem itemOpen = this.frame.initFileOpenMenuItem();
        menuFile.add(itemOpen);
        final MenuItem itemClose = this.frame.initFileCloseMenuItem();
        menuFile.add(itemClose);

        menuFile.addSeparator();

        final MenuItem itemQuit = initFileQuitMenuItem();
        menuFile.add(itemQuit);

//        TODO setHelpMenu();
//        TODO "about..." box

        add(menuFile);

        frame.setMenuBar(this);
    }

    private MenuItem initFileQuitMenuItem() {
        final MenuItem itemQuit = new MenuItem("Exit");
        itemQuit.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
        itemQuit.addActionListener(this::fileExit);
        return itemQuit;
    }

    private void fileExit(final ActionEvent e) {
        LOG.info("File/Exit menu item chosen: {}", e.paramString());
        this.frame.quitIfSafe();
    }
}
