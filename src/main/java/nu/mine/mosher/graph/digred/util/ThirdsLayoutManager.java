package nu.mine.mosher.graph.digred.util;

import java.awt.*;

/**
 * Lays out two components side by side, with the left one being one third the width
 * of the parent container, and the right one being two thirds.
 */
public class ThirdsLayoutManager extends LayoutManagerAdapter {
    @Override
    public void layoutContainer(final Container parent) {
        if (parent.getComponentCount() != 2) {
            throw new IllegalStateException("This layout manager only works with two components.");
        }

        final Dimension full = parent.getSize();
        final int height = parent.getHeight();

        final int third1 = (int)(Math.round(Math.rint(full.width * (1.0D / 3.0D))));
        parent.getComponent(0).setSize(third1, height);

        final int third2 = (int)(Math.round(Math.rint(full.width * (2.0D / 3.0D))));
        parent.getComponent(1).setLocation(third1, 0);
        parent.getComponent(1).setSize(third2, height);
    }
}
