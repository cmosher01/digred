import nu.mine.mosher.graph.digred.Digred;

module nu.mine.mosher.graph.digred {
    exports nu.mine.mosher.graph.digred;
    exports nu.mine.mosher.graph.digred.util;
    provides ch.qos.logback.classic.spi.Configurator with Digred.LogConfig;
//    requires log.files;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.apache.commons.logging;
    requires log4j;
    requires jul.to.slf4j;
    requires java.logging;
    requires java.desktop;
    requires java.prefs;
    requires org.antlr.antlr4.runtime;
    requires org.neo4j.driver;
}
