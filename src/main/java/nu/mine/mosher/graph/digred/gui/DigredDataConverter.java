package nu.mine.mosher.graph.digred.gui;

import nu.mine.mosher.graph.digred.schema.Entity;
import nu.mine.mosher.graph.digred.schema.*;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.types.*;

import java.awt.*;
import java.util.*;

// In Neo4j, property values cannot be NULL; rather, the property simply wouldn't exist.
// However, a STRING property can exist and be empty.
// Currently we treat empty string as NULL
// TODO Do we need to distinguish between NULL and empty string?

/**
 *
 *  Logic for various data conversions, including display strings.
 *
 *
 *
 *  NOTES ON DISPLAY LOGIC:
 *
 *  ----------
 *  Display of DATA TYPE names:
 *
 *  T1. dropdown types (node) "(:Persona)"
 *  T2. dropdown types (relationship) "(:Persona)-[:HAD_ROLE_IN]->(:Event)"
 *  T3. "Add" relationship link button "(this:Persona)-[add:HAD_ROLE_IN]->(:Event)"
 *  T3. choose box type title (rel) (same as T3)
 *
 *  ----------
 *  Display of ENTITY (node, relationship) names:
 *
 *  Areas in the GUI:
 *  1. entity list (node/rel)
 *  2. detail heading (node/rel)
 *  3. list of links (rel)
 *  4. link button (node)
 *  5. choose box list (node)
 *
 *  When viewing a NODE:
 *  N1. list of entities (NODE)
 *  N2. header ID at top of entity detail form (NODE)
 *  R3. list of links (REL)
 *  N1. choose entity popup dialog (same as N1) (NODE)
 *
 *  When viewing a RELATIONSHIP:
 *  R1. list of entities (REL)
 *  R2. header ID at top of entity detail (REL)
 *  N3. header buttons at top entity detail (NODE)
 *
 *  algorithms:
 *  ("mod" = "last modification timestamp")
 *  N1: if name is non-blank, then "name", else "mod: (:Type{ID:###})" (mod if available)
 *  N2: "(this:Type{ID:###})"
 *  N3: if name is non-blank, then "name", else "(:Type{ID:###})" (special case of N1)
 *
 *  R1: if name is non-blank, then "N3 - name -> N3", else "mod: N3 - [:TYPE{ID:###}] -> N3"
 *  R2: "[this:TYPE{ID:###}]"
 *  R3: "(this:Type) - RA -> N3" or "N3 - RA -> (this:Type)"
 *  where RA: if name is non-blank, then "name", else "[:TYPE{ID:###}]"
 */
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

    public static String displayNode(final Node node, final DigredModel model, final boolean withMod) {
        final var defType = defTypeOf(node, model);
        var name = nameOrBlank(node, defType);
        if (name.isBlank()) {
            name = displayEntityWithID("", defType, node.id());
            if (withMod) {
                final var mod = modOrBlank(node, defType);
                if (!mod.isBlank()) {
                    name = mod+": "+name;
                }
            }
        }
        return name;
    }

    public static String displayRel(final Relationship rel, final Node tail, final Node head, final DigredModel model) {
        final var defType = defTypeOf(rel, tail, head, model);
        var name = nameOrBlank(rel, defType);
        final var displayTail = displayNode(tail, model, false);
        final var displayHead = displayNode(head, model, false);

        if (!name.isBlank()) {
            return displayTail+" - "+name+" -> "+displayHead;
        }

        var mod = modOrBlank(rel, defType);
        if (!mod.isBlank()) {
            mod = mod + ": ";
        }

        name = displayEntityWithID("", defType, rel.id());
        return mod+displayTail+" - "+name+" -> "+displayHead;
    }

    public static String displayThisNodeType(final Vertex vertex) {
        return "(this:"+vertex.typename()+")";
    }

    public static String displayOutgoingRel(final Relationship rel, final Node tail, final Node head, final DigredModel model) {
        final var defType = defTypeOf(rel, tail, head, model);
        final var name = nameOrBlank(rel, defType);
        final var relWithID = displayEntityWithID("", defType, rel.id());
        final var headWithID = displayNode(head, model, false);
        final var defTypeThis = displayThisNodeType(defTypeOf(tail, model));
        return String.format("%s - %s -> %s", defTypeThis, name.isBlank() ? relWithID : name, headWithID);
    }

    public static String displayIncomingRel(final Relationship rel, final Node tail, final Node head, final DigredModel model) {
        final var defType = defTypeOf(rel, tail, head, model);
        final var name = nameOrBlank(rel, defType);
        final var relWithID = displayEntityWithID("", defType, rel.id());
        final var tailWithID = displayNode(tail, model, false);
        final var defTypeThis = displayThisNodeType(defTypeOf(head, model));
        return String.format("%s - %s -> %s", tailWithID, name.isBlank() ? relWithID : name, defTypeThis);
    }

    public static String displayEntityWithID(final String varname, final Entity entity, final long id) {
        return String.format(
            "%s%s:%s{ID:%d}%s",
                entity.vertex() ? "(" : "[",
                varname,
                entity.typename(),
                id,
                entity.vertex() ? ")" : "]"
        );
    }

    public static String displayOutgoingType(final Edge e) {
        return displayShortType("this", e.tail()) + " - " + displayShortType("add", e) + " -> " + displayShortType("", e.head());
    }

    public static String displayIncomingType(final Edge e) {
        return displayShortType("", e.tail()) + " - " + displayShortType("add", e) + " -> " + displayShortType("this", e.head());
    }

    public static String displaySimpleRelType(final Entity e) {
        if (e.vertex()) {
            return displayShortType("", e);
        }
        final var edge = (Edge)e;
        return displayShortType("", edge.tail()) + " - " + displayShortType("", e) + " -> " + displayShortType("", edge.head());
    }

    public static String displayShortType(final String varname, final Entity entity) {
        return String.format(
            "%s%s:%s%s",
            entity.vertex() ? "(" : "[",
            varname,
            entity.typename(),
            entity.vertex() ? ")" : "]"
        );
    }

    public static Vertex defTypeOf(final Node node, final DigredModel model) {
        final var defTypeName = node.labels().iterator().next();
        final var defEntity = model.schema.of(defTypeName);
        if (!defEntity.vertex()) {
            throw new IllegalStateException(); // TODO ?
        }
        return defEntity;
    }

    public static Edge defTypeOf(final Relationship rel, final Node tail, final Node head, final DigredModel model) {
        final var defTypeName = rel.type();
        final var defTypeNameTail = tail.labels().iterator().next();
        final var defTypeNameHead = head.labels().iterator().next();
        final var defEntity = model.schema.of(defTypeName, defTypeNameTail, defTypeNameHead);
        if (defEntity.vertex()) {
            throw new IllegalStateException(); // TODO ?
        }
        return defEntity;
    }

    public static String nameOrBlank(final org.neo4j.driver.types.Entity nodeOrRel, final Entity defEntity) {
        final var defPropName = defEntity.propOf(DataType._DIGRED_NAME);
        if (defPropName.isPresent()) {
            final var name = DigredDataConverter.asGoodString(nodeOrRel.get(defPropName.get().key()));
            if (name.isPresent()) {
                return name.get();
            }
        }

        // TODO if no _DIGRED_NAME, use smart algorithm:
        //  if one STRING prop use it, else if one TEXT use, else if one prop convert and use, else blank

        return "";
    }

    public static String modOrBlank(final org.neo4j.driver.types.Entity nodeOrRel, final Entity defEntity) {
        final var defPropName = defEntity.propOf(DataType._DIGRED_MODIFIED);
        if (defPropName.isPresent()) {
            final var mod = DigredDataConverter.displayValueOf(nodeOrRel.get(defPropName.get().key()));
            if (!mod.isEmpty()) {
                // TODO truncate to second?
                return mod;
            }
        }

        return "";
    }

    public static Optional<String> asGoodString(final Value v) {
        if (Objects.nonNull(v) && !v.isNull() && TypeConstructor.STRING.covers(v) && !v.asString().isBlank()) {
            return Optional.of(v.asString());
        }
        return Optional.empty();
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
