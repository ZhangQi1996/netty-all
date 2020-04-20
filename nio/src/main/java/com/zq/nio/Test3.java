package com.zq.nio;

import com.zq.utils.FileUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Test3 {
    private static final String fileName = "nio_test.txt";

    public static void main(String[] args) throws IOException {

        byte[] bytes = new byte[512];
        int len = 0;
        String path = FileUtil.getFilePathByClassLoader(fileName);

        FileInputStream in = new FileInputStream(path);
        len = in.read(bytes);
        in.close();

        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.put(
                new String(bytes, 0, len, StandardCharsets.UTF_8)
                        .toUpperCase()
                        .getBytes(StandardCharsets.UTF_8)
        );

        FileOutputStream out = new FileOutputStream(path);
        // write -> read
        buffer.flip();
        out.getChannel().write(buffer);
        out.close();
    }
}
