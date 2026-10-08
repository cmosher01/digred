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

package nu.mine.mosher.graph.digred.datastore;

import org.neo4j.driver.*;
import org.neo4j.driver.types.*;
import org.slf4j.Logger;
import org.slf4j.*;

import java.net.URI;
import java.time.*;
import java.util.Objects;

public class DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);

    public static final URI NEO = URI.create("neo4j://localhost:7687");

    private Driver database;

    public static class DataTypes {
        public static final Type NEO_NULL = Values.NULL.type();
        public static final Type NEO_INTEGER = Values.value(Long.MAX_VALUE).type();
        public static final Type NEO_FLOAT = Values.value(Double.MAX_VALUE).type();
        public static final Type NEO_DATE = Values.value(LocalDate.now()).type();
        public static final Type NEO_TIME = Values.value(OffsetTime.now()).type();
        public static final Type NEO_DATETIME = Values.value(ZonedDateTime.now()).type();
        public static final Type NEO_STRING = Values.value("").type();
        public static final Type NEO_BOOLEAN = Values.value(true).type();
    }

    public void connect(final URI uri, final String username, final String password) {
        disconnect();
        LOG.info("Connecting to database: {}", uri);
        this.database = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public boolean ping() {
        try {
            LOG.trace("Verifying connectivity to the database...");
            this.database.verifyConnectivity();
            LOG.trace("Successfully verified connectivity to the database.");
        } catch (final Exception e) {
            LOG.warn("Error connecting to database", e);
            return false;
        }

        return true;
    }

    public void disconnect() {
        if (connected()) {
            LOG.info("Disconnecting from database...");
            this.database.close();
            this.database = null;
            LOG.info("Disconnecting from database is complete.");
        } else {
            LOG.info("Did not disconnect from database, because no existing connection was found.");
        }
    }

    public Session session() {
        return this.database.session();
    }

    public boolean connected() {
        return Objects.nonNull(this.database);
    }

    public static URI uriDefault() {
        return NEO;
    }

    public static String usernameDefault() {
        return "neo4j";
    }

    public static String passwordDefault() {
        return "MarryCavalierLevitateGigahertz";
    }

//    public TypeSystem types() {
//        return this.database.defaultTypeSystem();
//    }
}
