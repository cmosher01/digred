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

public class Prop {
    private final String key;
    private final DataType type;

    public Prop(String key, DataType type) {
        this.key = key;
        this.type = type;
    }

    public String key() {
        return this.key;
    }

    public DataType type() {
        return this.type;
    }

    public void decompile(final PrintWriter out) {
        out.print(key());
        out.print(" : ");
        out.print(type());
    }

    public String display() {
        return key()+" : "+type();
    }
}
