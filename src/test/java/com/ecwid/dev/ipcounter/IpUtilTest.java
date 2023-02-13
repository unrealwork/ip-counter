package com.ecwid.dev.ipcounter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IpUtilTest {

    @ParameterizedTest
    @CsvSource( {
            "ips15.in,15",
            "ips100K.in,100000"
    })
    void countUnique(String testFileName, int expected) throws FileNotFoundException {
        Path path = ResourceUtil.testResourcePath(testFileName);
        assertEquals(expected, IpUtil.countUnique(path));
    }

    @Test
    void testNonExistingFile() {
        Path p = Paths.get("non-exist.in");
        assertThrows(FileNotFoundException.class,
                () -> IpUtil.countUnique(p)
        );
    }
}
