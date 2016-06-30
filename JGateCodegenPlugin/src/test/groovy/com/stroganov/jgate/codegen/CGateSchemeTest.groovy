package com.stroganov.jgate.codegen

import org.junit.Test

import java.util.stream.StreamSupport

import static org.junit.Assert.*

/**
 * Created by stroganov on 19.06.2016.
 */
class CGateSchemeTest {
    @Test
    public void test() {
        println('CGateScheme test');
        assertEquals(['FortsFutInfoRepl', 'FortsOptInfoRepl', 'RtsIndexRepl'],
                CGateScheme.fromIterable([new File('fut_info.ini'), new File('rts_index.ini')]).collect { it.name });

        assertEquals(['fut_sess_contents', 'sys_events', 'opt_sess_contents', 'sys_events'],
                StreamSupport.stream(
                        CGateScheme.fromIterable([new File('fut_info.ini')]).spliterator(),
                        false).flatMap { StreamSupport.stream(it.tables.spliterator(), false) }.toArray().toList()
        );
    }
}
