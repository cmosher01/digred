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

import nu.mine.mosher.graph.digred.Digred;

module nu.mine.mosher.graph.digred {
    exports nu.mine.mosher.graph.digred;
    exports nu.mine.mosher.graph.digred.util;
    provides ch.qos.logback.classic.spi.Configurator with Digred.LogConfig;
    requires log.files;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.apache.commons.logging;
    requires log4j.over.slf4j;
    requires jul.to.slf4j;
    requires java.logging;
    requires java.desktop;
    requires java.prefs;
    requires org.antlr.antlr4.runtime;
    requires org.neo4j.driver;
}
