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

import nu.mine.mosher.graph.digred.schema.Entity;

import java.util.Optional;

public class DigredEntityIdent {
    private final Entity type;
    private final Optional<String> id;
// TODO   private final Optional<ZonedDateTime> mod;

    public DigredEntityIdent(final Entity type, final String id) {
        this.type = type;
        this.id = Optional.of(id);
    }

    public DigredEntityIdent(final Entity type) {
        this.type = type;
        this.id = Optional.empty();
    }

    public DigredEntityIdent with(final String id) {
        return new DigredEntityIdent(type(), id);
    }

    public Entity type() {
        return this.type;
    }

    public Optional<String> id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "DigredEntityIdent{" +
            "type=" + type.typename() +
            ", ID=" + id +
            '}';
    }
}
