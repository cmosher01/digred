package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.Entity;

import java.util.Optional;

public class DigredEntityIdent {
    private final Entity type;
    private final Optional<Long> id;
// TODO   private final Optional<ZonedDateTime> mod;

    public DigredEntityIdent(final Entity type, final long id) {
        this.type = type;
        this.id = Optional.of(id);
    }

    public DigredEntityIdent(final Entity type) {
        this.type = type;
        this.id = Optional.empty();
    }

    public DigredEntityIdent with(final long id) {
        return new DigredEntityIdent(type(), id);
    }

    public Entity type() {
        return this.type;
    }

    public Optional<Long> id() {
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
