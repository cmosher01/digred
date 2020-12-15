package nu.mine.mosher.graph.digred.tools;

import nu.mine.mosher.graph.digred.schema.*;

/**
 * TODO do we even need this class at all?
 *
 * Handles special built-in properties: pk, created, modified:
 *
 * TODO: version
 *
 * On creation:
 *     pk = random UUID
 *     version = 1
 *     created = modified = now
 *
 * On update:
 *     version++
 *     modified = now
 *
 * The methods act as filters for an operation that builds a Cypher query string.
 * Pass in the property and the value to set, then the method will override that value
 * in the case of a specially handled property.
 *
 */
public class DigredProperties {
//    public MapValue newEntity(final List<Prop> props) {
//        final Map<String, Value> map = new HashMap<>();
//
//        props.forEach(prop -> {
//            switch (prop.key()) {
//                case "pk" -> UUID.randomUUID().toString();
//            }
//        });
//
//        props.forEach(prop -> map.put(prop.key(), newValueFor(prop)));
//        return new MapValue(map);
//    }

    public String cypherForNewProp(final Prop prop, final String orElse) {
        if (!prop.key().startsWith("_digred_")) {
            return orElse;
        }

        if (prop.key().equals("_digred_pk") && prop.type() == DataType.STRING) {
            return "apoc.create.uuid()";
        }
        if (prop.key().equals("created") && prop.type() == DataType.DATETIME) {
            return "timestamp()";
        }
        if (prop.key().equals("modified") && prop.type() == DataType.DATETIME) {
            return "timestamp()";
        }
//        if (prop.key().equals("version") && prop.type() == DataType.INTEGER) {
//            return "1";
//        }
        return orElse;
    }

    public String cypherForUpdateProp(final Prop prop, final String orElse) {
        if (prop.key().equals("modified") && prop.type() == DataType.DATETIME) {
            return "timestamp()";
        }
//        if (prop.key().equals("version") && prop.type() == DataType.INTEGER) {
//            return nodeVarName+".version+1";
//        }
        return orElse;
    }
}
