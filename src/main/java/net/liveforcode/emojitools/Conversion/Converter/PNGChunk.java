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

package net.liveforcode.emojitools.conversion.Converter;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PNGChunk {

    static int[] crc_table = null;
    final String name;
    byte[] CRC;
    int length;
    byte[] data;

    public PNGChunk(int length, String name, byte[] CRC) {
        this(length, name, CRC, null);
    }

    public PNGChunk(int length, String name, byte[] data, byte[] CRC) {
        this.length = length;
        this.name = name;
        this.CRC = CRC;
        this.data = data;
    }

    public static PNGChunk generateChunk(DataInputStream input) throws IOException {
        int length = readPngInt(input);

        byte[] data = new byte[4];
        input.readFully(data);
        String name = new String(data, "ASCII");

        byte[] dataBuffer = new byte[length];
        input.readFully(dataBuffer);

        byte[] CRC = new byte[4];
        input.readFully(CRC);

        if (name.equalsIgnoreCase("IHDR")) {
            return new PNGIHDRChunk(length, name, dataBuffer, CRC);
        }

        return new PNGChunk(length, name, dataBuffer, CRC);
    }

    private static void writeInt(byte[] dest, int index, int value) {
        dest[index] = (byte) ((value & 0xff000000) >> 24);
        dest[index + 1] = (byte) ((value & 0xff0000) >> 16);
        dest[index + 2] = (byte) ((value & 0xff00) >> 8);
        dest[index + 3] = (byte) (value & 0xff);
    }

    private static int readPngInt(DataInputStream input) throws IOException {
        final byte[] buffer = new byte[4];
        input.readFully(buffer);
        return readInt(buffer, 0);
    }

    static int readInt(byte[] dest, int index) {
        return ((dest[index++] & 0xFF) << 24)
                | ((dest[index++] & 0xFF) << 16)
                | ((dest[index++] & 0xFF) << 8)
                | (dest[index] & 0xFF);
    }

    public static void writeCRC(byte[] data, int index) {
        int chunklen = readInt(data, index);
        int sum = ~CRCChecksum(data, index + 4, 4 + chunklen);
        writeInt(data, index + 8 + chunklen, sum);
    }

    private static int CRCChecksum(byte[] buffer, int offset, int length) {
        int c = 0xffffffff;
        int n;
        if (crc_table == null) {
            int mkc;
            int mkn, mkk;
            crc_table = new int[256];
            for (mkn = 0; mkn < 256; mkn++) {
                mkc = mkn;
                for (mkk = 0; mkk < 8; mkk++) {
                    if ((mkc & 1) == 1) {
                        mkc = 0xedb88320 ^ (mkc >>> 1);
                    } else {
                        mkc = mkc >>> 1;
                    }
                }
                crc_table[mkn] = mkc;
            }
        }
        for (n = offset; n < length + offset; n++) {
            c = crc_table[(c ^ buffer[n]) & 0xff] ^ (c >>> 8);
        }
        return c;
    }

    public void calculateCRC() {
        byte[] toCalculate = new byte[4 + length];
        byte[] nameBytes = this.name.getBytes();
        System.arraycopy(nameBytes, 0, toCalculate, 0, 4); //Add Name into calculation
        System.arraycopy(data, 0, toCalculate, 4, data.length); //Add data into calculation
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(~CRCChecksum(toCalculate, 0, toCalculate.length));
        this.CRC = buffer.array();
    }

    public void writeToStream(FileOutputStream outStream) throws IOException {
        byte length[] = new byte[4];
        length[0] = (byte) ((this.length & 0xFF000000) >> 24);
        length[1] = (byte) ((this.length & 0xFF0000) >> 16);
        length[2] = (byte) ((this.length & 0xFF00) >> 8);
        length[3] = (byte) (this.length & 0xFF);

        outStream.write(length);
        outStream.write(name.getBytes("ASCII"));
        outStream.write(data, 0, this.length);
        outStream.write(CRC);
    }

    public byte[] getCRC() {
        return CRC;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
