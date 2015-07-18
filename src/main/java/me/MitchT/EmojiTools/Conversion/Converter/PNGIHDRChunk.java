package me.MitchT.EmojiTools.Conversion.Converter;

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
