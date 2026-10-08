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

public class DigraphSchema {
    private final List<Entity> e;

    public DigraphSchema(final List<Entity> e) {
        this.e = e;
    }

    public List<Entity> e() {
        return this.e;
    }

    private static final String DIGRED_COMMON = "_DIGRED_COMMON";

    public static boolean common(final String label) {
        return label.equalsIgnoreCase(DigraphSchema.DIGRED_COMMON);
    }



    public void decompile(final PrintWriter out) {
        e().forEach(entity -> {
            entity.decompile(out);
            out.println();
            entity.props().forEach(p -> {
                out.print("    ");
                p.decompile(out);
                out.println();
            });
        });
    }

    public void applyCommon() {
        final var entityCommon = e().stream().filter(Entity::common).findAny();
        if (entityCommon.isEmpty()) {
            return;
        }

        final var propsCommon = entityCommon.get().props();
        if (propsCommon.isEmpty()) {
            return;
        }

        e().remove(entityCommon.get());

        e().forEach(e -> e.addExtraProps(propsCommon));
   }

    public HashMap<Vertex,List<Edge>> edgesOut() {
        final var map = new HashMap<Vertex, List<Edge>>();
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                map.put(v, new ArrayList<>());
            }
        }
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                map.get(e.tail()).add(e);
            }
        }
        return map;
    }

    public HashMap<Vertex,List<Edge>> edgesIn() {
        final var map = new HashMap<Vertex, List<Edge>>();
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                map.put(v, new ArrayList<>());
            }
        }
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                map.get(e.head()).add(e);
            }
        }
        return map;
    }

    public Edge of(final String type, final String tail, final String head) {
        for (final var n : e()) {
            if (!n.vertex()) {
                final var e = (Edge)n;
                if (e.typename().equals(type) && e.tail().typename().equals(tail) && e.head().typename().equals(head)) {
                    return e;
                }
            }
        }
        throw new IllegalStateException("Invalid edge: "+tail+"-"+type+"->"+head);
    }

    public Vertex of(final String label) {
        for (final var n : e()) {
            if (n.vertex()) {
                final var v = (Vertex)n;
                if (v.typename().equals(label)) {
                    return v;
                }
            }
        }
        throw new IllegalStateException("Invalid vertex: "+label);
    }
}
