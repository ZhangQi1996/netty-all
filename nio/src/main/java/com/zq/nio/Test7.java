package com.zq.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.stream.IntStream;

public class Test7 {
    // nio非阻塞编程
    public static void main(String[] args) throws IOException {
        int[] ports = IntStream.rangeClosed(5000, 5004).toArray(); // 5个

        Selector selector = Selector.open();



    }
}
