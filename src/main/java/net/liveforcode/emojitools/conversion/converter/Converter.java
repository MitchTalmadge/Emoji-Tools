/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package net.liveforcode.emojitools.conversion.converter;

import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.GZIPException;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;
import net.liveforcode.emojitools.EmojiTools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class Converter {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final byte COLOR_GREYSCALE = 0;
    private static final byte COLOR_TRUECOLOR = 2;
    private static final byte COLOR_INDEXED = 3;
    private static final byte COLOR_GREYALPHA = 4;
    private static final byte COLOR_TRUEALPHA = 6;
    private File sourceFile;
    private boolean CgBItoRGBA;
    private ArrayList<PNGChunk> chunks;

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    public static Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = (WritableRaster) image.getData();
        raster.setPixels(0, 0, width, height, pixels);
        return image;
    }

    public void convertFile(File source, boolean CgBItoRGBA) {
        this.sourceFile = source;
        this.CgBItoRGBA = CgBItoRGBA;

        this.chunks = new ArrayList<>();

        if (source.isDirectory()) {
            for (File file : source.listFiles())
                convertFile(file, CgBItoRGBA);
        } else if (source.getName().toLowerCase().endsWith(".png")) {
            try {
                readChunks();

                if ((CgBItoRGBA && getChunkByName("CgBI") != null) || (!CgBItoRGBA && getChunkByName("CgBI") == null)) {

                    if (!CgBItoRGBA) {
                        PNGChunk chunk = new PNGChunk(4, "CgBI", new byte[]{0x50, 0x00, 0x20, 0x02}, new byte[]{0x2B, (byte) 0xD5, (byte) 0xB3, 0x7F});
                        chunks.add(0, chunk);
                    }

                    PNGIHDRChunk IHDRChunk = (PNGIHDRChunk) getChunkByName("IHDR");
                    if (IHDRChunk != null) {
                        int maxInflateBuffer = (((IHDRChunk.getColorType() == COLOR_TRUECOLOR) ? 3 : 4) * (IHDRChunk.getWidth()) + 1) * IHDRChunk.getHeight();
                        byte[] outputBuffer = new byte[maxInflateBuffer];

                        convertDataChunk(IHDRChunk, outputBuffer, maxInflateBuffer);

                        writePng();
                    }
                }
            } catch (IOException e) {
                EmojiTools.submitError(Thread.currentThread(), e);
            }
        }
    }

    private void readChunks() throws FileNotFoundException {
        try (DataInputStream input = new DataInputStream(new FileInputStream(sourceFile))) {

            byte[] PNGHeader = new byte[8];
            input.readFully(PNGHeader);

            if ((PNGHeader[0] == -119) && (PNGHeader[1] == 0x50) && (PNGHeader[2] == 0x4e) && (PNGHeader[3] == 0x47)
                    && (PNGHeader[4] == 0x0d) && (PNGHeader[5] == 0x0a) && (PNGHeader[6] == 0x1a) && (PNGHeader[7] == 0x0a)) {

                PNGChunk chunk;

                do {
                    chunk = PNGChunk.generateChunk(input);
                    chunks.add(chunk);
                }
                while (!chunk.getName().equalsIgnoreCase("IEND"));
            }
        } catch (IOException e) {
            EmojiTools.submitError(Thread.currentThread(), e);
        }
    }

    private void convertDataChunk(PNGIHDRChunk IHDRChunk, byte[] outputBuffer, int maxInflateBuffer) throws IOException {

        inflate(outputBuffer, maxInflateBuffer);

        //Apply row filters
        for (int y = 0; y < IHDRChunk.getHeight(); y++) {
            byte rowFilter = outputBuffer[y * ((IHDRChunk.getWidth() * ((IHDRChunk.getColorType() == COLOR_TRUECOLOR) ? 3 : 4)) + 1)];
            PNGFilterHandler.filter(rowFilter & 0xFF, outputBuffer, y, (IHDRChunk.getWidth() * ((IHDRChunk.getColorType() == COLOR_TRUECOLOR) ? 3 : 4)) + 1, false, IHDRChunk.getColorType() == COLOR_TRUEALPHA);
        }

        //Convert TrueColor to TrueAlpha (RGB -> RGBA)
        if (IHDRChunk.getColorType() == COLOR_TRUECOLOR) {
            IHDRChunk.setColorType(COLOR_TRUEALPHA);
            IHDRChunk.calculateCRC();

            maxInflateBuffer = (4 * (IHDRChunk.getWidth()) + 1) * IHDRChunk.getHeight();

            byte[] conversionBuffer = new byte[maxInflateBuffer];

            int i = 0;

            for (int y = 0; y < IHDRChunk.getHeight(); y++) {
                conversionBuffer[y * ((IHDRChunk.getWidth() * 4) + 1)] = outputBuffer[y * ((IHDRChunk.getWidth() * 3) + 1)];
                i++;
                for (int x = 1; x < (IHDRChunk.getWidth() * 3) + 1; x += 3) {
                    conversionBuffer[i++] = outputBuffer[y * ((IHDRChunk.getWidth() * 3) + 1) + x];
                    conversionBuffer[i++] = outputBuffer[y * ((IHDRChunk.getWidth() * 3) + 1) + x + 1];
                    conversionBuffer[i++] = outputBuffer[y * ((IHDRChunk.getWidth() * 3) + 1) + x + 2];
                    conversionBuffer[i++] = (byte) 0xFF;
                }
            }

            outputBuffer = conversionBuffer;
        }

        // Switch the color
        if (CgBItoRGBA) {
            int index = 0;
            int r, g, b, a;

            for (int y = 0; y < IHDRChunk.getHeight(); y++) {
                index++;
                for (int x = 0; x < IHDRChunk.getWidth(); x++) {
                    b = outputBuffer[index] & 0xFF;
                    g = outputBuffer[index + 1] & 0xFF;
                    r = outputBuffer[index + 2] & 0xFF;
                    a = outputBuffer[index + 3] & 0xFF;

                    if (a == 0) {
                        outputBuffer[index] = (byte) (r & 0xFF);
                        outputBuffer[index + 1] = (byte) (g & 0xFF);
                        outputBuffer[index + 2] = (byte) (b & 0xFF);
                    } else {
                        outputBuffer[index] = (byte) (((r * 255) / a) & 0xFF);
                        outputBuffer[index + 1] = (byte) (((g * 255) / a) & 0xFF);
                        outputBuffer[index + 2] = (byte) (((b * 255) / a) & 0xFF);
                    }
                    index += 4;
                }
            }
        } else {
            int index = 0;
            int r, g, b, a;

            for (int y = 0; y < IHDRChunk.getHeight(); y++) {
                index++;
                for (int x = 0; x < IHDRChunk.getWidth(); x++) {
                    r = outputBuffer[index] & 0xFF;
                    g = outputBuffer[index + 1] & 0xFF;
                    b = outputBuffer[index + 2] & 0xFF;
                    a = outputBuffer[index + 3] & 0xFF;

                    outputBuffer[index] = (byte) ((b * a / 0xFF) & 0xFF);
                    outputBuffer[index + 1] = (byte) ((g * a / 0xFF) & 0xFF);
                    outputBuffer[index + 2] = (byte) ((r * a / 0xFF) & 0xFF);
                    index += 4;
                }
            }
        }

        //Set all row filters to None (0)
        for (int y = 0; y < IHDRChunk.getHeight(); y++) {
            outputBuffer[y * ((IHDRChunk.getWidth() * 4) + 1)] = 0x00;
        }

        Deflater deflater = deflate(outputBuffer, outputBuffer.length, maxInflateBuffer);

        // Put the result in the first IDAT chunk (the only one to be written out)
        PNGChunk firstDataTrunk = getChunkByName("IDAT");

        byte[] deflated = deflater.getNextOut();
        long totalOut = deflater.getTotalOut();

        CRC32 crc32 = new CRC32();
        crc32.update(firstDataTrunk.getName().getBytes());
        crc32.update(deflated, 0, (int) totalOut);
        long lCRCValue = crc32.getValue();

        firstDataTrunk.setData(deflated);
        firstDataTrunk.getCRC()[0] = (byte) ((lCRCValue & 0xFF000000) >> 24);
        firstDataTrunk.getCRC()[1] = (byte) ((lCRCValue & 0xFF0000) >> 16);
        firstDataTrunk.getCRC()[2] = (byte) ((lCRCValue & 0xFF00) >> 8);
        firstDataTrunk.getCRC()[3] = (byte) (lCRCValue & 0xFF);
        firstDataTrunk.setLength((int) totalOut);
    }

    private long inflate(byte[] conversionBuffer, int maxInflateBuffer) throws GZIPException {
        Inflater inflater = new Inflater(this.CgBItoRGBA ? -15 : 15);

        for (PNGChunk chunk : chunks) {
            if (!chunk.getName().equalsIgnoreCase("IDAT"))
                continue;
            inflater.setInput(chunk.getData(), true);
        }

        inflater.setOutput(conversionBuffer);

        int result;
        try {
            result = inflater.inflate(JZlib.Z_NO_FLUSH);
            checkResultStatus(result);
        } finally {
            inflater.inflateEnd();
        }

        return inflater.getTotalOut();
    }

    private Deflater deflate(byte[] buffer, int length, int maxInflateBuffer) throws GZIPException {
        Deflater deflater = new Deflater(JZlib.Z_BEST_COMPRESSION, this.CgBItoRGBA ? 15 : -15);
        deflater.setInput(buffer, 0, length, false);

        int maxDeflateBuffer = maxInflateBuffer + 1024;
        byte[] deBuffer = new byte[maxDeflateBuffer];
        deflater.setOutput(deBuffer);

        int result = deflater.deflate(JZlib.Z_FULL_FLUSH);
        checkResultStatus(result);

        return deflater;
    }

    private void checkResultStatus(int result) throws GZIPException {
        switch (result) {
            case JZlib.Z_OK:
            case JZlib.Z_STREAM_END:
                break;

            case JZlib.Z_NEED_DICT:
                throw new GZIPException("Z_NEED_DICT - " + result);
            case JZlib.Z_DATA_ERROR:
                throw new GZIPException("Z_DATA_ERROR - " + result);
            case JZlib.Z_MEM_ERROR:
                throw new GZIPException("Z_MEM_ERROR - " + result);
            case JZlib.Z_STREAM_ERROR:
                throw new GZIPException("Z_STREAM_ERROR - " + result);
            case JZlib.Z_BUF_ERROR:
                throw new GZIPException("Z_BUF_ERROR - " + result);
            default:
                throw new GZIPException("inflater error: " + result);
        }
    }

    private void writePng() throws IOException {
        try (FileOutputStream outStream = new FileOutputStream(sourceFile)) {
            byte[] pngHeader = {-119, 80, 78, 71, 13, 10, 26, 10};
            outStream.write(pngHeader);
            boolean dataWritten = false;
            for (PNGChunk chunk : chunks) {
                // Skip Apple specific and misplaced CgBI chunk
                if (chunk.getName().equalsIgnoreCase("CgBI") && this.CgBItoRGBA) {
                    continue;
                }

                // Only write the first IDAT chunk as they have all been put together now
                if ("IDAT".equalsIgnoreCase(chunk.getName())) {
                    if (dataWritten) {
                        continue;
                    } else {
                        dataWritten = true;
                    }
                }

                chunk.writeToStream(outStream);
            }
            outStream.flush();
        }
    }

    private PNGChunk getChunkByName(String name) {
        for (PNGChunk chunk : chunks) {
            if (chunk.getName().equalsIgnoreCase(name))
                return chunk;
        }
        return null;
    }

}
