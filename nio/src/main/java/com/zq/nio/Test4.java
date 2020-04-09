package com.zq.nio;

import com.zq.utils.Util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Test4 {

    public static void main(String[] args) throws IOException {
        String path = Util.getFilePathByClassLoader("nio_test.txt");
        RandomAccessFile file = new RandomAccessFile(path, "rw");

        FileChannel channel = file.getChannel();
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, (byte) 'X');
        mappedByteBuffer.put(4, (byte) 'Y');

        file.close();
    }
}
