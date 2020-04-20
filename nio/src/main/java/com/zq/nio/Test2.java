package com.zq.nio;


import com.zq.utils.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test2 {
    public static void main(String[] args) throws IOException {

        FileInputStream in = new FileInputStream(
                FileUtil.getFilePathByClassLoader("nio_test.txt")
        );
        FileChannel channel = in.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(512);
        // write
        channel.read(buffer);
        // flip write to read
        buffer.flip();

        while (buffer.hasRemaining()) {
            System.out.println("character: " + (char) buffer.get());
        }

        in.close();


    }
}
