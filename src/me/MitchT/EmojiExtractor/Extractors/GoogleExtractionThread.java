package me.MitchT.EmojiExtractor.Extractors;

import me.MitchT.EmojiExtractor.EmojiExtractor;
import me.MitchT.EmojiExtractor.ExtractionManager;
import me.MitchT.EmojiExtractor.ExtractionUtilites;
import me.MitchT.EmojiExtractor.GUI.ProgressPanel;

import java.io.*;
import java.util.List;

public class GoogleExtractionThread extends ExtractionThread {
    private static final File emojisDir = new File(EmojiExtractor.getRootDirectory() + "/ExtractedEmojis");

    private List<String> tableNames;
    private List<Integer> tableOffsets;
    private List<Integer> tableLengths;

    private short numGlyphs = 0;
    private String[] glyphNames;

    private ExtractionManager extractionManager;
    private ProgressPanel progressPanel;

    public GoogleExtractionThread(File font, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, ExtractionManager extractionManager, ProgressPanel progressPanel) {
        super(font);

        this.tableNames = tableNames;
        this.tableOffsets = tableOffsets;
        this.tableLengths = tableLengths;

        this.extractionManager = extractionManager;
        this.progressPanel = progressPanel;

        progressPanel.setShowTimeRemaining(false);
        progressPanel.setShowStatusMessage(true);
    }

    @Override
    public void run() {
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            if (!emojisDir.exists()) {
                emojisDir.mkdir();
            }

            setProgressStatusMessage("Searching for Emojis - Please wait until complete!");

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

            int CBLCIndex = tableNames.indexOf("CBLC");
            if (CBLCIndex > -1) {
                int CBLCOffset = tableOffsets.get(CBLCIndex);
                int CBLCLength = tableLengths.get(CBLCIndex);

                inputStream.seek(CBLCOffset);

                b = new byte[4];
                inputStream.readFully(b);
                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                    extractionManager.showMessagePanel("Invalid 'CBLC' table! Contact developer for help.");
                    inputStream.close();
                    return;
                }

                inputStream.skipBytes(44);

                b = new byte[2];
                inputStream.readFully(b);
                short beginGlyphID = ExtractionUtilites.getShortFromBytes(b);

                inputStream.readFully(b);
                short endGlyphID = ExtractionUtilites.getShortFromBytes(b);

                //Get number of strikes, and scan for PNG files.
                int CBDTIndex = tableNames.indexOf("CBDT");
                if (CBDTIndex > -1) {
                    int CBDTOffset = tableOffsets.get(CBDTIndex);
                    int CBDTLength = tableLengths.get(CBDTIndex);

                    inputStream.seek(CBDTOffset);

                    b = new byte[4];
                    inputStream.readFully(b);
                    if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                        extractionManager.showMessagePanel("Invalid 'CBDT' table! Contact developer for help.");
                        inputStream.close();
                        return;
                    }

                    for (int i = 0; i < endGlyphID - beginGlyphID; i++) {
                        inputStream.skipBytes(5);
                        b = new byte[4];
                        inputStream.readFully(b);

                        int glyphLength = ExtractionUtilites.getIntFromBytes(b);
                        if (glyphLength > 0) {
                            b = new byte[glyphLength];
                            System.out.println("Extracting Emoji #" + i + " to '" + glyphNames[i + beginGlyphID] + ".png'");
                            setProgressStatusMessage("Extracting Emoji #" + i + " to '" + glyphNames[i + beginGlyphID] + ".png'");
                            updateProgress((int) ((i / (float) (endGlyphID - beginGlyphID) * 100)));
                            FileOutputStream outputStream = new FileOutputStream(new File(emojisDir, glyphNames[i + beginGlyphID] + ".png"));
                            b = new byte[glyphLength];
                            inputStream.readFully(b);
                            outputStream.write(b);
                            outputStream.close();
                        }
                    }
                } else {
                    extractionManager.showMessagePanel("Could not find 'CBDT' table! Contact developer for help.");
                    inputStream.close();
                    return;
                }
            } else {
                extractionManager.showMessagePanel("Could not find 'CBLC' table! Contact developer for help.");
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

    private void updateProgress(int progress) {
        progressPanel.setProgress(progress);
    }

    private void setProgressStatusMessage(String message) {
        progressPanel.setStatusMessage(message);
    }

}
