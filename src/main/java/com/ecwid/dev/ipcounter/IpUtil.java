package com.ecwid.dev.ipcounter;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Contains utility methods related to IPs.
 *
 * @author unrealwork
 */
public class IpUtil {
    private IpUtil() {
    }


    /**
     * Efficiently count unique IPV4 addresses in specified file
     *
     * @param p path to file with IPs
     * @return count of unique IPV4 addresses in the file.
     * @throws FileNotFoundException in case if provided file doesn't exist
     */
    public static long countUnique(Path p) throws FileNotFoundException {
        if (Files.notExists(p)) {
            throw new FileNotFoundException("Specified file: " + p + " doesn't exist");
        }
        FileUniqueIpCounter fileUniqueIpCounter = new FileUniqueIpCounter(p);
        return fileUniqueIpCounter.count();
    }
}
