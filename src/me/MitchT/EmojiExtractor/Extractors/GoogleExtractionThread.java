package me.MitchT.EmojiExtractor.Extractors;

import me.MitchT.EmojiExtractor.EmojiExtractor;
import me.MitchT.EmojiExtractor.ExtractionManager;
import me.MitchT.EmojiExtractor.ExtractionUtilites;
import me.MitchT.EmojiExtractor.GUI.ProgressPanel;

import java.io.*;
import java.util.List;

public class GoogleExtractionThread extends ExtractionThread {
    private static final File emojisDir = new File(EmojiExtractor.getRootDirectory() + "/ExtractedEmojis");

    private long currentBytePos = 0;

    private List<String> tableNames;
    private List<Integer> tableOffsets;
    private List<Integer> tableLengths;

    private short numGlyphs = 0;
    private String[] glyphNames;

    private ExtractionManager extractionManager;
    private ProgressPanel progressPanel;

    private long startTime = 0;
    private long currTime = 0;

    public GoogleExtractionThread(File font, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, ExtractionManager extractionManager, ProgressPanel progressPanel) {
        super(font);

        this.tableNames = tableNames;
        this.tableOffsets = tableOffsets;
        this.tableLengths = tableLengths;

        this.extractionManager = extractionManager;
        this.progressPanel = progressPanel;
    }

    @Override
    public void run() {
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            if (!emojisDir.exists()) {
                emojisDir.mkdir();
            }

            startTime = System.currentTimeMillis();

            //Get numGlyphs, ordinal numbers, and glyphNames from post table
            int postIndex = tableNames.indexOf("post");
            if (postIndex > -1) {
                int offset = tableOffsets.get(postIndex);
                int length = tableLengths.get(postIndex);

                inputStream.seek(offset);

                b = new byte[4];
                inputStream.readFully(b);

                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                    extractionManager.showMessagePanel("Invalid 'post' table format! Contact developer for help.");
                    inputStream.close();
                    return;
                }

                inputStream.skipBytes(28);
                b = new byte[2];
                inputStream.readFully(b);

                numGlyphs = ExtractionUtilites.getShortFromBytes(b);
                short[] ordinalNumbers = new short[numGlyphs];

                short numNewGlyphs = 0;

                for (int i = 0; i < numGlyphs; i++) {
                    inputStream.readFully(b);
                    ordinalNumbers[i] = ExtractionUtilites.getShortFromBytes(b);
                    if (ordinalNumbers[i] > 257)
                        numNewGlyphs++;
                }

                String[] extraNames = new String[numNewGlyphs];

                for (int i = 0; i < numNewGlyphs; i++) {

                    short nameLen = (short) inputStream.read();
                    b = new byte[nameLen];
                    inputStream.readFully(b);
                    extraNames[i] = ExtractionUtilites.getStringFromBytes(b);
                }

                //Build list of names for GlyphIDs
                glyphNames = new String[numGlyphs];
                for (int i = 0; i < numGlyphs; i++) {
                    if (ordinalNumbers[i] <= 257) {
                        glyphNames[i] = standardOrderNames[ordinalNumbers[i]];
                    } else {
                        glyphNames[i] = extraNames[ordinalNumbers[i] - 258];
                    }
                }
            } else {
                extractionManager.showMessagePanel("Could not find 'post' table! Contact developer for help.");
                inputStream.close();
                return;
            }

            //Get number of strikes, and scan for PNG files.
            int sbixIndex = tableNames.indexOf("sbix");
            if (sbixIndex > -1) {
                int offset = tableOffsets.get(sbixIndex);
                int length = tableLengths.get(sbixIndex);

                inputStream.seek(offset);

                inputStream.skipBytes(4);

                b = new byte[4];
                inputStream.readFully(b);

                int numStrikes = ExtractionUtilites.getIntFromBytes(b);
                int[] strikeOffsets = new int[numStrikes];
                for (int i = 0; i < numStrikes; i++) {
                    inputStream.readFully(b);
                    strikeOffsets[i] = ExtractionUtilites.getIntFromBytes(b);
                }

                //TODO: Figure out how to convert rgbl to png.. for now, use last strike..
                inputStream.seek(offset + strikeOffsets[strikeOffsets.length - 1]);
                inputStream.skipBytes(4);

                int[] glyphOffsets = new int[numGlyphs];
                int[] glyphLengths = new int[numGlyphs];
                b = new byte[4];

                for (int i = 0; i < numGlyphs; i++) {
                    inputStream.readFully(b);
                    glyphOffsets[i] = ExtractionUtilites.getIntFromBytes(b);
                }

                for (int i = 0; i < numGlyphs; i++) {
                    if (i + 1 == numGlyphs)
                        glyphLengths[i] = length - strikeOffsets[strikeOffsets.length - 1] - glyphOffsets[i];
                    else
                        glyphLengths[i] = glyphOffsets[i + 1] - glyphOffsets[i];
                }

                inputStream.seek(offset + strikeOffsets[strikeOffsets.length - 1]);
                inputStream.skipBytes(glyphOffsets[0]);

                for (int i = 0; i < numGlyphs; i++) {
                    if (glyphLengths[i] > 0) {
                        inputStream.skipBytes(4);
                        b = new byte[4];
                        inputStream.readFully(b);
                        if (ExtractionUtilites.getStringFromBytes(b).equals("png ")) {
                            System.out.println("Extracting Glyph #" + i + " to '" + glyphNames[i] + ".png'");
                            FileOutputStream outputStream = new FileOutputStream(new File(emojisDir, glyphNames[i] + ".png"));
                            b = new byte[glyphLengths[i] - 8];
                            inputStream.readFully(b);
                            outputStream.write(b);
                            outputStream.close();
                        }
                    }
                }
            } else {
                extractionManager.showMessagePanel("Could not find 'sbix' table! Contact developer for help.");
                inputStream.close();
                return;
            }
            inputStream.close();

            System.out.println("No more Emojis to extract! All done! :)");
            extractionManager.showMessagePanel("No more Emojis to extract! All done! :)");
        } catch (FileNotFoundException e) {
            System.out.println(this.font.getName() + " not found!");
            extractionManager.showMessagePanel(this.font.getName() + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress() {
        this.currTime = System.currentTimeMillis();
        progressPanel.setProgress(currentBytePos, this.font.length());
        progressPanel.setTimeRemaining(currentBytePos, this.font.length(), currTime, startTime);
    }

}
