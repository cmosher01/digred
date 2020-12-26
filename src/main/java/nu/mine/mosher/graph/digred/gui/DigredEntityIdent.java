package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.*;

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

//    public int schemaTypeIndex(final DigraphSchema schema) {
//        if (schema.e().isEmpty()) {
//            throw new IllegalStateException();
//        }
//
//        if (this.type.isEmpty()) {
//            return 0;
//        }
//
//        return schema.e().indexOf(this.type.get());
//    }

    public Entity type() {
        return this.type;
    }

    public Optional<Long> id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "DigredEntityIdent{" +
            "type=" + type.display() +
            ", ID=" + id +
            '}';
    }
}
