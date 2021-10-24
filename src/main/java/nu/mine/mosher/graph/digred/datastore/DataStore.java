package nu.mine.mosher.graph.digred.datastore;

import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.*;

import java.net.URI;
import java.util.*;

public class DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);

    private static final URI NEO = URI.create("neo4j://localhost:7687");

    private Driver database;

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
        return "";
    }
}
