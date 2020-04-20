package com.zq.netty.zerocopy;

import com.zq.utils.FileUtil;
import com.zq.utils.LogUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewIOCli {

    private static void runInTradition(FileChannel fileChannel, SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        long startTime = System.currentTimeMillis();
        int len = 0, total = 0;

        while ((len = fileChannel.read(buffer)) != -1) {
            buffer.flip();
            socketChannel.write(buffer);

            total += len;

            buffer.clear();
        }

        LogUtil.LOG.info("[traditional mean] the total bytes read are {}, costs {} ms totally.",
                total, System.currentTimeMillis() - startTime);
    }

    private static void runInMap(FileChannel fileChannel, SocketChannel socketChannel) throws IOException {
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

        long startTime = System.currentTimeMillis();

        int total = socketChannel.write(buffer);
        LogUtil.LOG.info("[the mean based mmap] the total bytes read are {}, costs {} ms totally.",
                total, System.currentTimeMillis() - startTime);
    }

    private static void runInTransferring(FileChannel fileChannel, SocketChannel socketChannel) throws IOException {
        long startTime = System.currentTimeMillis();

        long total = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
        LogUtil.LOG.info("[the mean based sendfile] the total bytes read are {}, costs {} ms totally.",
                total, System.currentTimeMillis() - startTime);
    }

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = null;
        RandomAccessFile file = null;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(8899));
//            socketChannel.configureBlocking(true);

            file = new RandomAccessFile(OldCli.FROM_FILE_PATH, "r");
            FileChannel fileChannel = file.getChannel();

            runInTradition(fileChannel, socketChannel);  // traditional mean, 2700ms
            runInMap(fileChannel, socketChannel);   // the mean based mmap, 1500ms
            runInTransferring(fileChannel, socketChannel); // the mean based sendfile, 170ms

        } finally {
            FileUtil.close(socketChannel);
            FileUtil.close(file);
        }
    }
}
