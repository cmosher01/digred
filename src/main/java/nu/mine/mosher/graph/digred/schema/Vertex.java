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

package nu.mine.mosher.graph.digred.schema;

import java.io.PrintWriter;
import java.util.*;

// (:label{props})
public class Vertex implements Entity {
    private final String label;
    private final List<Prop> props;

    public Vertex(String label, List<Prop> props) {
        this.label = label;
        this.props = props;
    }

    public String label() {
        return this.label;
    }

    public List<Prop> props() {
        return this.props;
    }

    @Override
    public boolean common() {
        return DigraphSchema.common(label());
    }

    @Override
    public void addExtraProps(List<Prop> props) {
        props().addAll(props);
    }

    @Override
    public boolean vertex() {
        return true;
    }

    @Override
    public String typename() {
        return label();
    }

    @Override
    public void decompile(final PrintWriter out) {
        out.print(typename());
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Vertex)) {
            return false;
        }
        final Vertex that = (Vertex)object;
        return label().equals(that.label());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label());
    }

    @Override
    public Optional<Prop> propOf(final DataType dataType) {
        for (final Prop p : props()) {
            if (p.type() == dataType) {
                return Optional.of(p);
            }
        }
        return  Optional.empty();
    }
}
