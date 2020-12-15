package nu.mine.mosher.graph.digred.gui;

import ch.qos.logback.classic.*;
import org.slf4j.*;
import org.slf4j.Logger;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

public class DigredGui {
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



    private DigredGui() {
        LOG.info("Starting up GUI, on thread: {}", Thread.currentThread().getName());
    }

    private void init() {
        this.events.set(Thread.currentThread());

        final DigredFrame frame = DigredFrame.create();
        DigredMenuBar.create(frame);

        frame.updateViewFromModel();
    }

    private static void initAwtLogging() {
        final LoggerContext ctx = (LoggerContext)LoggerFactory.getILoggerFactory();
        ctx.getLogger("sun.awt").setLevel(Level.INFO);
        ctx.getLogger("java.awt").setLevel(Level.INFO);
    }
}
