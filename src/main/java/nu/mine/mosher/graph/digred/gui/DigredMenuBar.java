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

        add(menuFile);



        final Menu menuDatabase = new Menu("Database");

        final MenuItem itemConnect = this.frame.initDatabaseConnectMenuItem();
        menuDatabase.add(itemConnect);
        final MenuItem itemDisconnect = this.frame.initDatabaseDisconnectMenuItem();
        menuDatabase.add(itemDisconnect);

        add(menuDatabase);




//        TODO setHelpMenu();
//        TODO "about..." box

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
