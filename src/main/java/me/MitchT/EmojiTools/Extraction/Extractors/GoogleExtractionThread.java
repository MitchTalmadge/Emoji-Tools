package me.MitchT.EmojiTools.Extraction.Extractors;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.Extraction.ExtractionUtilites;
import me.MitchT.EmojiTools.GUI.ExtractionDialog;

import java.io.*;
import java.util.List;

public class GoogleExtractionThread extends ExtractionThread {

    private final List<String> tableNames;
    private final List<Integer> tableOffsets;
    private final List<Integer> tableLengths;

    private final ExtractionManager extractionManager;
    private final ExtractionDialog extractionDialog;

    public GoogleExtractionThread(File font, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, ExtractionManager extractionManager, ExtractionDialog extractionDialog) {
        super(font, extractionDirectory, "GoogleExtractionThread");

        this.tableNames = tableNames;
        this.tableOffsets = tableOffsets;
        this.tableLengths = tableLengths;

        this.extractionManager = extractionManager;
        this.extractionDialog = extractionDialog;

    }

    @Override
    public void run() {
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            if (!extractionDirectory.exists()) {
                extractionDirectory.mkdir();
            }

            appendToStatus("Searching for Emojis - Please wait until complete!");

            //Get numGlyphs, ordinal numbers, and glyphNames from post table
            int postIndex = tableNames.indexOf("post");
            if (postIndex > -1) {
                int postOffset = tableOffsets.get(postIndex);
                int postLength = tableLengths.get(postIndex);

                inputStream.seek(postOffset);

                b = new byte[4];
                inputStream.readFully(b);

                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                    extractionManager.showMessageDialog("Invalid 'post' table format! Contact developer for help.");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                inputStream.skipBytes(28);
                b = new byte[2];
                inputStream.readFully(b);

                short numGlyphs = ExtractionUtilites.getShortFromBytes(b);
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
                String[] glyphNames = new String[numGlyphs];
                for (int i = 0; i < numGlyphs; i++) {
                    if (ordinalNumbers[i] <= 257) {
                        glyphNames[i] = standardOrderNames[ordinalNumbers[i]];
                    } else {
                        glyphNames[i] = extraNames[ordinalNumbers[i] - 258];
                    }
                }

                int CBLCIndex = tableNames.indexOf("CBLC");
                if (CBLCIndex > -1) {
                    int CBLCOffset = tableOffsets.get(CBLCIndex);
                    int CBLCLength = tableLengths.get(CBLCIndex);

                    inputStream.seek(CBLCOffset);

                    b = new byte[4];
                    inputStream.readFully(b);
                    if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                        extractionManager.showMessageDialog("Invalid 'CBLC' table! Contact developer for help.");
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
                            extractionManager.showMessageDialog("Invalid 'CBDT' table! Contact developer for help.");
                            inputStream.close();
                            return;
                        }

                        System.out.println("# Glyphs: " + numGlyphs);
                        System.out.println("# New Glyphs: " + numNewGlyphs);
                        System.out.println("# Emojis to Extract: " + (endGlyphID - beginGlyphID));

                        for (int i = beginGlyphID; i <= endGlyphID; i++) {
                            if (!running) {
                                inputStream.close();
                                extractionDialog.dispose();
                                return;
                            }
                            inputStream.skipBytes(5);
                            b = new byte[4];
                            inputStream.readFully(b);

                            int glyphLength = ExtractionUtilites.getIntFromBytes(b);
                            if (glyphLength > 0) {
                                b = new byte[glyphLength];
                                System.out.println("Extracting Emoji #" + i + " to '" + glyphNames[i] + ".png'");
                                appendToStatus("Extracting Emoji #" + i + " to '" + glyphNames[i] + ".png'");
                                updateProgress((int) ((i - beginGlyphID) / (float) (endGlyphID - beginGlyphID) * 100));
                                FileOutputStream outputStream = new FileOutputStream(new File(extractionDirectory, glyphNames[i] + ".png"));
                                b = new byte[glyphLength];
                                inputStream.readFully(b);
                                outputStream.write(b);
                                outputStream.close();
                            }
                        }
                    } else {
                        extractionManager.showMessageDialog("Could not find 'CBDT' table! Contact developer for help.");
                        inputStream.close();
                        extractionDialog.dispose();
                        return;
                    }
                } else {
                    extractionManager.showMessageDialog("Could not find 'CBLC' table! Contact developer for help.");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }
            } else {
                extractionManager.showMessageDialog("Could not find 'post' table! Contact developer for help.");
                inputStream.close();
                extractionDialog.dispose();
                return;
            }

            inputStream.close();

            extractionDialog.dispose();
        } catch (FileNotFoundException e) {
            System.out.println(this.font.getName() + " not found!");
            extractionManager.showMessageDialog(this.font.getName() + " not found!");
        } catch (IOException e) {
            EmojiTools.submitError(Thread.currentThread(), e);
        }
    }

    private void updateProgress(int progress) {
        extractionDialog.setProgress(progress);
    }

    private void appendToStatus(String message) {
        extractionDialog.appendToStatus(message);
    }

}
