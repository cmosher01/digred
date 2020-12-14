package nu.mine.mosher.graph.digred.datastore;

import nu.mine.mosher.graph.digred.schema.DataType;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.slf4j.*;
import org.slf4j.Logger;

import java.net.URI;
import java.util.*;

public class DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);

    public static final URI NEO = URI.create("neo4j://localhost:7687");

    public static final Map<DataType, TypeConstructor> mapDataTypes = Map.of(
        DataType.INTEGER, TypeConstructor.INTEGER,
        DataType.FLOAT, TypeConstructor.FLOAT,
        DataType.STRING, TypeConstructor.STRING,
        DataType.BOOLEAN, TypeConstructor.BOOLEAN,
        DataType.DATE, TypeConstructor.DATE,
        DataType.TIME, TypeConstructor.TIME,
        DataType.DATETIME, TypeConstructor.DATE_TIME,
        DataType.DURATION, TypeConstructor.DURATION,

        DataType.TEXT, TypeConstructor.STRING,
        DataType.UUID, TypeConstructor.STRING
    );

    private Driver database;

    public void connect(final URI uri, final String username, final String password) {
        disconnect();
        LOG.info("Connecting to database: {}", uri);
        this.database = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void disconnect() {
        if (Objects.nonNull(this.database)) {
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
}
