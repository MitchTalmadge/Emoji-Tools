package me.MitchT.EmojiExtractor;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExtractionUtilites
{
    
    public static short getShortFromBytes(byte[] bytes)
    {
        return ByteBuffer.wrap(bytes).getShort();
    }
    
    public static int getIntFromBytes(byte[] bytes)
    {
        return ByteBuffer.wrap(bytes).getInt();
    }
    
    public static String getStringFromBytes(byte[] bytes)
    {
        return new String(bytes);
    }
    
    public static boolean compareBytes(byte[] bytes, byte ... compare)
    {
        return Arrays.equals(bytes, compare);
    }

}
