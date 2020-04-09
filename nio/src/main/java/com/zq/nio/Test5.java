package com.zq.nio;

import com.zq.utils.Util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Test5 {
    public static void main(String[] args) throws IOException {
        String path = Util.getFilePathByClassLoader("nio_test.txt");
        RandomAccessFile file = new RandomAccessFile(path, "rw");

        FileChannel channel = file.getChannel();
        // 获得文件锁
        FileLock fileLock = channel.lock(0, 2, true);

        System.out.println("Valid: " + fileLock.isValid());
        System.out.println("Lock type: " + fileLock.isShared());

        fileLock.release(); // 释放锁
        file.close();
    }
}
