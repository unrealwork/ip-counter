package com.ecwid.dev.ipcounter;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IpCounterTest {

    @Test
    void testValid() {
        Path path = ResourceUtil.testResourcePath("ips15.in");
        assertDoesNotThrow(() -> IpCounter.main(path.toString()));
    }

    @Test
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, IpCounter::main);
    }
}
