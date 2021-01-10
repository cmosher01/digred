grammar Digred ;
/*
    Vertex
        prop:TYPE
    Vertex EDGE Vertex
        prop:TYPE
*/



@header {
    import java.util.*;
    import java.util.stream.*;
}



schema returns [DigraphSchema scm] locals [
        List<Entity> rE = new ArrayList<>(),
        Map<String, Vertex> mV = new HashMap<>()
    ]
    : entity[$rE,$mV]* EOF { $scm = new DigraphSchema(List.copyOf($rE)); }
    ;

entity[List<Entity> rE, Map<String, Vertex> mV]
    : vertex    { $rE.add($vertex.v); $mV.put($vertex.v.label(), $vertex.v); }
    | edge[$mV] { $rE.add($edge.e); }
    | NL
    ;

vertex returns [Vertex v]
    : label=ID NL props+=prop* {
        $v = new Vertex(
            $label.text,
            $props.stream().map(p -> p.p).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList()));
    }
    ;

edge[Map<String, Vertex> mV] returns [Edge e]
    : tail=ID type=ID head=ID NL props+=prop* {
        $e = new Edge(
            $type.text,
            $props.stream().map(p -> p.p).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList()),
            $mV.get($tail.text),
            $mV.get($head.text));
    }
    ;

prop returns [Prop p]
    : key=ID TYPE_DELIM TYPE NL { $p = new Prop($key.text, DataType.valueOf($TYPE.text)); }
    | NL { $p = null; }
    ;



TYPE : 'INTEGER' | 'FLOAT' | 'STRING' | 'TEXT' | 'BOOLEAN' | 'DATE' | 'TIME' | 'DATETIME' | 'DURATION' | 'UUID' |
       '_DIGRED_PK' | '_DIGRED_VERSION' | '_DIGRED_CREATED' | '_DIGRED_MODIFIED' | '_DIGRED_NAME';

ID : [A-Za-z0-9_]+ ;

TYPE_DELIM : ':' ;

NL : '\r'? '\n' ;

BLOCK_COMMENT : '/*' .*?  '*/' -> skip ;
LINE_COMMENT : '#' ~[\r\n]* -> skip ;

WS : [ \t]+ -> skip ;
