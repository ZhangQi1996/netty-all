package com.zq.nio;

import java.nio.IntBuffer;
import java.security.SecureRandom;

public class Test1 {
    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(10);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(random.nextInt(20));
        }

        // 切换读写
        buffer.flip();

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }
}
