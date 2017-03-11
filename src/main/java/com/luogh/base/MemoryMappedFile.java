package com.luogh.base;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.System.out;

/**
 * @author luogh
 */
public class MemoryMappedFile {
    private static final int count = 10485760; //10MB
    public static void main(String[] arg) throws Exception {
        RandomAccessFile rf = new RandomAccessFile("E://bitmap_test", "rw");
        // Mapping a file into memory
        MappedByteBuffer buffer = rf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, count);

        out.println((char)buffer.get());
        buffer.rewind();
        // writing into Memory Mapped File
        for (int i=0; i<count; i++) {
            buffer.put((byte)'A');
        }
        out.println("Writing to Memory Mapped File is completed.");

        // reading from memory file in Java
        for (int i=0;i < 10; i++) {
            out.println((char)buffer.get(i));
        }
        out.println("Reading from Memory Mapped File is completed.");

        rf.close();
    }

}
