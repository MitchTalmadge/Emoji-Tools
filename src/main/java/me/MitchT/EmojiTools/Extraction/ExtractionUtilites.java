package me.MitchT.EmojiTools.Extraction;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class ExtractionUtilites {

    public static String getByteString(RandomAccessFile inputStream, int numBytesToRead) {
        byte[] bytes = new byte[numBytesToRead];
        try {
            inputStream.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }

    public static boolean compareBytes(RandomAccessFile inputStream, byte... compare) {
        byte[] bytes = new byte[compare.length];
        try {
            inputStream.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.equals(bytes, compare);
    }

}
