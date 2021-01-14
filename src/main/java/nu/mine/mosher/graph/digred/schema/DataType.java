package nu.mine.mosher.graph.digred.schema;

public enum DataType {
    INTEGER,
    FLOAT,
    STRING,
    TEXT,
    BOOLEAN,
    DATE,
    TIME,
    DATETIME,
    DURATION,
    UUID,

    // digred-specific datatypes:

    /**
     * UUID primary key for entity, intended as a permanent,
     * primary key, UUID unique identifier for the entity.
     *
     * Digred sets this upon creation, but does not used it otherwise.
     */
    _DIGRED_PK,

    /** >= 1, incremented upon modification */
    _DIGRED_VERSION,

    /** creation timestamp */
    _DIGRED_CREATED,
    /** last modified timestamp */
    _DIGRED_MODIFIED,

    /** Short name for the entity. Digred uses this to display the entity in lists. */
    _DIGRED_NAME
}
