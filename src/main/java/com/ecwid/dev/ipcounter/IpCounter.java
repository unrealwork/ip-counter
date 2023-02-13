package com.ecwid.dev.ipcounter;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point of the App.
 *
 * @author unrealwork
 */
public class IpCounter {
    private IpCounter() {

    }

    /**
     * Count IPV4 addresses in specified text file and print an answer in standard output.
     *
     * @param args contain path to text file as first argument.
     * @throws FileNotFoundException in case if specified file is not found.
     */
    @SuppressWarnings("squid:S106")
    public static void main(String... args) throws FileNotFoundException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Path to file with IPs should be provided");
        }
        Path p = Paths.get(args[0]);

        long count = IpUtil.countUnique(p);
        System.out.println(count);
    }
}
