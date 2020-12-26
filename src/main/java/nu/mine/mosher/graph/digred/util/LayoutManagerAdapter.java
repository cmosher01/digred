package nu.mine.mosher.graph.digred.util;

import java.awt.*;

public class LayoutManagerAdapter implements LayoutManager {
    @Override
    public void addLayoutComponent(final String name, final Component comp) {
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        return minimumLayoutSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return parent.getSize();
    }

    @Override
    public void layoutContainer(final Container parent) {
    }
}
