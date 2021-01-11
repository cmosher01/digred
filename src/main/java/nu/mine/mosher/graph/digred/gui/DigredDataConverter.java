package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.*;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.value.NullValue;

import java.awt.*;
import java.util.*;

// In Neo4j, property values cannot be NULL; rather, the property simply wouldn't exist.
// However, a STRING property can exist and be empty.
// Currently we treat empty string as NULL
// TODO Do we need to distinguish between NULL and empty string?
public class DigredDataConverter {
    public static ArrayList<String> digredCypherProps(final Entity e) {
        final var cyProps = new ArrayList<String>();
        e.props().forEach(prop -> {
            final Optional<String> v = newValueOf(prop);
            v.ifPresent(s -> cyProps.add(prop.key() + ": " + s));
        });
        return cyProps;
    }

    private static Optional<String> newValueOf(final Prop prop) {
        return Optional.ofNullable(switch (prop.type()) {
            case _DIGRED_PK -> "apoc.create.uuid()";
            case _DIGRED_VERSION -> "1";
            case _DIGRED_CREATED, _DIGRED_MODIFIED -> "datetime.realtime()";
            default -> null;
        });
    }

    public static String displayValueOf(final Value value) {
        if (Objects.isNull(value)) {
            return "";
        }
        if (TypeConstructor.STRING.covers(value)) {
            return value.asString();
        }
        if (TypeConstructor.INTEGER.covers(value)) {
            return Long.toString(value.asLong(), 10);
        }
        if (TypeConstructor.FLOAT.covers(value)) {
            return Double.toString(value.asDouble());
        }
        if (TypeConstructor.DATE_TIME.covers(value)) {
            return value.asZonedDateTime().toString();
        }
        if (TypeConstructor.NULL.covers(value)) {
            return "";
        }
        return "[cannot convert value of type "+value.type().name()+" for display]";
    }

    public static void setComponentValue(final Value val, final Prop prop, final Component cmp) {
        switch (prop.type()) {
            case BOOLEAN -> ((Checkbox)cmp).setState(!val.isNull() && val.asBoolean());
            default -> ((TextComponent)cmp).setText(displayValueOf(val));
        }
    }

    public static Component componentOfValue(final Value val, final Prop prop) {
        return switch (prop.type()) {
            case TEXT -> {
                final TextArea t = new TextArea(displayValueOf(val));
                t.setEditable(!readonly(prop) && canConvert(val));
                yield t;
            }
            case BOOLEAN -> {
                final Checkbox c = new Checkbox(prop.key(), !val.isNull() && val.asBoolean());
                c.setEnabled(!readonly(prop) && canConvert(val));
                yield c;
            }
            default -> {
                final TextField t = new TextField(displayValueOf(val));
                t.setEditable(!readonly(prop) && canConvert(val));
                yield t;
            }
        };
    }

    public static Value valueOfComponent(final Component cmp, final Prop prop) {
        return switch (prop.type()) {
            // TODO handle invalid format
            case INTEGER -> Values.value(Long.parseLong(((TextComponent)cmp).getText(),10));
            case FLOAT -> Values.value(Double.parseDouble(((TextComponent)cmp).getText()));
            case BOOLEAN -> Values.value(((Checkbox)cmp).getState());
            // TODO other types
            default -> {
                final var t = ((TextComponent)cmp).getText();
                yield t.isEmpty() ? NullValue.NULL : Values.value(t);
            }
        };
    }

    private static boolean canConvert(final Value value) {
        return
            TypeConstructor.STRING.covers(value) ||
                TypeConstructor.INTEGER.covers(value) ||
                TypeConstructor.FLOAT.covers(value) ||
                TypeConstructor.DATE_TIME.covers(value) ||
                TypeConstructor.BOOLEAN.covers(value) ||
                TypeConstructor.NULL.covers(value);
        /*
            TODO: handle remaining datatypes:
            DATE
            TIME
            LOCAL_TIME
            LOCAL_DATE_TIME
            DURATION
            POINT
         */
    }

    private static final Set<DataType> setReadOnly = Set.of(
        DataType._DIGRED_PK,
        DataType._DIGRED_CREATED,
        DataType._DIGRED_MODIFIED,
        DataType._DIGRED_VERSION
    );

    public static boolean readonly(final Prop prop) {
        return setReadOnly.contains(prop.type());
    }
}
