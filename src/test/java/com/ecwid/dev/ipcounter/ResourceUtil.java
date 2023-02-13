package com.ecwid.dev.ipcounter;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtil {
    private ResourceUtil(){
        
    }

    static Path testResourcePath(String testFileName) {
        URL resource = IpUtilTest.class.getClassLoader().getResource(testFileName);
        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
