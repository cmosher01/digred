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

import java.net.*;
import java.util.jar.*;

public class Version {
    protected Version() {
    }

    public String version() {
        final String urlManifest = String.format("jrt:/%s/META-INF/MANIFEST.MF", getClass().getPackage().getName());
        try {
            final Manifest manifest = new Manifest(URI.create(urlManifest).toURL().openStream());
            return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (final Throwable e) {
            return "UNOFFICIAL VERSION";
        }
    }
}
