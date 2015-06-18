package me.MitchT.EmojiTools.Extraction.Extractors;

import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.Extraction.ExtractionUtilites;
import me.MitchT.EmojiTools.GUI.ExtractionDialog;

import java.io.*;
import java.util.List;

public class AppleExtractionThread extends ExtractionThread {

    private List<String> tableNames;
    private List<Integer> tableOffsets;
    private List<Integer> tableLengths;

    private ExtractionManager extractionManager;
    private ExtractionDialog extractionDialog;

    public AppleExtractionThread(File font, String exportDirectoryName, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, ExtractionManager extractionManager, ExtractionDialog extractionDialog) {
        super(font, exportDirectoryName);

        this.tableNames = tableNames;
        this.tableOffsets = tableOffsets;
        this.tableLengths = tableLengths;

        this.extractionManager = extractionManager;
        this.extractionDialog = extractionDialog;

        this.extractionDialog.setTimeRemainingVisible(false);

    }

    @Override
    public void run() {
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            if (!exportDir.exists()) {
                exportDir.mkdir();
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

                //Get number of strikes, and scan for PNG files.
                int sbixIndex = tableNames.indexOf("sbix");
                if (sbixIndex > -1) {
                    int sbixOffset = tableOffsets.get(sbixIndex);
                    int sxixLength = tableLengths.get(sbixIndex);

                    inputStream.seek(sbixOffset);

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
                    inputStream.seek(sbixOffset + strikeOffsets[strikeOffsets.length - 1]);
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
                            glyphLengths[i] = sxixLength - strikeOffsets[strikeOffsets.length - 1] - glyphOffsets[i];
                        else
                            glyphLengths[i] = glyphOffsets[i + 1] - glyphOffsets[i];
                    }

                    inputStream.seek(sbixOffset + strikeOffsets[strikeOffsets.length - 1]);
                    inputStream.skipBytes(glyphOffsets[0]);

                    for (int i = 0; i < numGlyphs; i++) {
                        if (!running) {
                            inputStream.close();
                            extractionDialog.dispose();
                            return;
                        }
                        updateProgress((int) ((i / (float) (numGlyphs)) * 100));

                        if (glyphLengths[i] > 0) {
                            inputStream.skipBytes(4);
                            b = new byte[4];
                            inputStream.readFully(b);
                            if (ExtractionUtilites.getStringFromBytes(b).equals("png ")) {
                                System.out.println("Extracting Emoji #" + i + " to '" + glyphNames[i] + ".png'");
                                appendToStatus("Extracting Emoji #" + i + " to '" + glyphNames[i] + ".png'");
                                FileOutputStream outputStream = new FileOutputStream(new File(exportDir, glyphNames[i] + ".png"));
                                b = new byte[glyphLengths[i] - 8];
                                inputStream.readFully(b);
                                outputStream.write(b);
                                outputStream.close();
                            }
                        }
                    }
                } else {
                    extractionManager.showMessageDialog("Could not find 'sbix' table! Contact developer for help.");
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

            System.out.println("No more Emojis to extract! All done! :)");
            extractionManager.showMessageDialog("No more Emojis to extract! All done! :)");
            extractionDialog.dispose();
        } catch (FileNotFoundException e) {
            System.out.println(this.font.getName() + " not found!");
            extractionManager.showMessageDialog(this.font.getName() + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(int progress) {
        extractionDialog.setProgress(progress);
    }

    private void appendToStatus(String message) {
        extractionDialog.appendToStatus(message);
    }

}
