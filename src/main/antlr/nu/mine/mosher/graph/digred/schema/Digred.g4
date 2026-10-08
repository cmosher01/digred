/*
 *     Copyright 2020, 2021, 2026, Christopher Alan Mosher, New York, New York, USA, <cmosher01@gmail.com>.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    : entity[$rE,$mV]* EOF { $scm = new DigraphSchema($rE); }
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
            $props.stream().map(p -> p.p).filter(Objects::nonNull).collect(Collectors.toList()));
    }
    ;

edge[Map<String, Vertex> mV] returns [Edge e]
    : tail=ID type=ID head=ID NL props+=prop* {
        $e = new Edge(
            $type.text,
            $props.stream().map(p -> p.p).filter(Objects::nonNull).collect(Collectors.toList()),
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
