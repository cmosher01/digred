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
     * Automatically set by digred, but not used by it otherwise
     */
    _DIGRED_PK,

    /** >= 1, incremented upon modification */
    _DIGRED_VERSION,

    /** creation timestamp */
    _DIGRED_CREATED,
    /** last modified timestamp */
    _DIGRED_MODIFIED,

    /** short name for the entity */
    _DIGRED_NAME
}
