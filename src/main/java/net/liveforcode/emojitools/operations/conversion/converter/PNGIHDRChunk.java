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

package net.liveforcode.emojitools.operations.conversion.converter;

public class PNGIHDRChunk extends PNGChunk {

    private final int width;
    private final int height;
    private byte bitDepth;
    private byte colorType;
    private byte compressionMethod;
    private byte filterMethod;
    private byte interlaceMethod;

    public PNGIHDRChunk(int length, String name, byte[] data, byte[] CRC) {
        super(length, name, data, CRC);

        width = readInt(data, 0);
        height = readInt(data, 4);
        bitDepth = data[8];
        colorType = data[9];
        compressionMethod = data[10];
        filterMethod = data[11];
        interlaceMethod = data[12];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public byte getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(byte bitDepth) {
        this.bitDepth = bitDepth;
        this.data[8] = bitDepth;
    }

    public byte getColorType() {
        return colorType;
    }

    public void setColorType(byte colorType) {
        this.colorType = colorType;
        this.data[9] = colorType;
    }

    public byte getCompressionMethod() {
        return compressionMethod;
    }

    public void setCompressionMethod(byte compressionMethod) {
        this.compressionMethod = compressionMethod;
        this.data[10] = compressionMethod;
    }

    public byte getFilterMethod() {
        return filterMethod;
    }

    public void setFilterMethod(byte filterMethod) {
        this.filterMethod = filterMethod;
        this.data[11] = filterMethod;
    }

    public byte getInterlaceMethod() {
        return interlaceMethod;
    }

    public void setInterlaceMethod(byte interlaceMethod) {
        this.interlaceMethod = interlaceMethod;
        this.data[12] = interlaceMethod;
    }

}
