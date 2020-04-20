package com.zq.utils;

import java.io.*;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Objects;

public class FileUtil {

    private FileUtil() {}

    public static String getFilePathByClassLoader(String fileName, ClassLoader cl) {
        URL url = Objects.requireNonNull(cl.getResource(fileName),
                String.format("By classloader %s, cannot find dest file.", cl));
        return url.getPath();
    }

    public static String getFilePathByClassLoader(String fileName) {
        return getFilePathByClassLoader(fileName, ClassLoader.getSystemClassLoader());
    }

    public static void copyFileFromAnotherFileByMap(String fromPath, String toPath) throws IOException {
        RandomAccessFile from = new RandomAccessFile(fromPath, "r");
        RandomAccessFile to = new RandomAccessFile(toPath, "rw");

        try {
            FileChannel fromChannel = from.getChannel();
            FileChannel toChannel = to.getChannel();

            FileLock toLock = toChannel.lock(0, Long.MAX_VALUE, false);
            FileLock fromLock = fromChannel.lock(0, Long.MAX_VALUE, true);

            MappedByteBuffer mapBuffer = fromChannel.map(FileChannel.MapMode.READ_ONLY, 0, fromChannel.size());
            fromChannel.write(mapBuffer);

            fromLock.release();
            toLock.release();
        } finally {
            FileUtil.close(from);
            FileUtil.close(to);
        }
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null)
            closeable.close();
    }

}
