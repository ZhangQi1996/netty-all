package com.zq.nio;

import com.zq.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Test8 {
    // 基于内存映射的文件内容的拷贝（零拷贝）
    public static void main(String[] args) throws IOException {
        String inFilePath = FileUtil.getFilePathByClassLoader("nio_in.txt");
        String outFilePath = FileUtil.getFilePathByClassLoader("nio_out.txt");

        RandomAccessFile inFile = new RandomAccessFile(inFilePath, "r");
        RandomAccessFile outFile = new RandomAccessFile(outFilePath, "rw");

        try {
            FileChannel inFileChannel = inFile.getChannel();
            FileChannel outFileChannel = outFile.getChannel();

            MappedByteBuffer mappedByteBuffer = inFileChannel
                    .map(FileChannel.MapMode.READ_ONLY, 0, inFileChannel.size());

            outFileChannel.write(mappedByteBuffer);
        } finally {
            inFile.close();
            outFile.close();
        }
    }
}
