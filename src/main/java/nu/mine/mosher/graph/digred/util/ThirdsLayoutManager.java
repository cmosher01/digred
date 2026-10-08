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
