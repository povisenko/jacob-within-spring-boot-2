package me.povisenko.jacob_within_spring_boot_2.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public abstract class ResourceUtil {

    public static String getResourceFileAsString(String fileName) {
        ClassLoader classLoader = ResourceUtil.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines()
                         .collect(Collectors.joining(System.lineSeparator()));
        }
        return null;
    }
}