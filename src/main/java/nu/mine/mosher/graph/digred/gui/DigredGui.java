package nu.mine.mosher.graph.digred.gui;

import ch.qos.logback.classic.*;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

public class DigredGui {
    public static DigredGui create() throws InvocationTargetException, InterruptedException {
        initAwtLogging();

        final AtomicReference<DigredGui> gui = new AtomicReference<>();
        EventQueue.invokeAndWait(() -> {
            gui.set(new DigredGui());
            gui.get().init();
        });
        return gui.get();
    }

    private static void initAwtLogging() {
        final LoggerContext ctx = (LoggerContext)LoggerFactory.getILoggerFactory();
        ctx.getLogger("sun.awt").setLevel(Level.INFO);
        ctx.getLogger("java.awt").setLevel(Level.INFO);
    }



    private final AtomicReference<Thread> events = new AtomicReference<>();
    private final Logger LOG = LoggerFactory.getLogger(DigredGui.class);

    // TODO make these 2 vars local:
    private final DigredFrame frame = new DigredFrame();
    private final DigredMenuBar menubar = new DigredMenuBar();

    private DigredGui() {
        LOG.info("Starting up GUI (on event thread)...");
    }

    private void init() {
        this.events.set(Thread.currentThread());

        this.frame.init();
        this.menubar.init(this.frame);
        this.frame.setMenuBar(this.menubar);
        this.frame.updateViewFromModel();
    }

    public void waitForEventThread() {
        try {
            this.events.get().join();
        } catch (final InterruptedException e) {
            LOG.error("thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
