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

import ch.qos.logback.classic.*;
import org.slf4j.Logger;
import org.slf4j.*;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

public class DigredGui {
    public static boolean COLOR_LAYOUT = false;

    private static final Logger LOG = LoggerFactory.getLogger(DigredGui.class);
    private final AtomicReference<Thread> events = new AtomicReference<>();



    public static DigredGui create() throws InvocationTargetException, InterruptedException {
        initAwtLogging();

        final AtomicReference<DigredGui> gui = new AtomicReference<>();
        EventQueue.invokeAndWait(() -> {
            gui.set(new DigredGui());
            gui.get().init();
        });
        return gui.get();
    }

    public void waitForEventThread() {
        try {
            this.events.get().join();
        } catch (final InterruptedException e) {
            LOG.error("thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static Color debugLayout(final Color c) {
        if (COLOR_LAYOUT) {
            return c;
        }
        return null;
    }



    private DigredGui() {
        LOG.info("Starting up GUI, on thread: {}", Thread.currentThread().getName());
    }

    private void init() {
        this.events.set(Thread.currentThread());

        final DigredFrame frame = DigredFrame.create();
        DigredMenuBar.create(frame);

        frame.updateMenus();
    }

    private static void initAwtLogging() {
        final LoggerContext ctx = (LoggerContext)LoggerFactory.getILoggerFactory();
        ctx.getLogger("sun.awt").setLevel(Level.INFO);
        ctx.getLogger("java.awt").setLevel(Level.INFO);
    }
}
