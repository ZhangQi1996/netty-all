package com.zq.utils;

import java.net.URL;
import java.util.Objects;

public class Util {

    private Util() {}

    public static String getFilePathByClassLoader(String fileName, ClassLoader cl) {
        URL url = Objects.requireNonNull(cl.getResource(fileName),
                String.format("By classloader %s, cannot find dest file.", cl));
        return url.getPath();
    }

    public static String getFilePathByClassLoader(String fileName) {
        return getFilePathByClassLoader(fileName, ClassLoader.getSystemClassLoader());
    }

}
