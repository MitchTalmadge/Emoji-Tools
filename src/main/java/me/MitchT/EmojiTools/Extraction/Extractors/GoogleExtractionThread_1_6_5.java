package me.MitchT.EmojiTools.Extraction.Extractors;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.Extraction.ExtractionUtilites;
import me.MitchT.EmojiTools.GUI.ExtractionDialog;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class GoogleExtractionThread_1_6_5 extends ExtractionThread {

    private final List<String> tableNames;
    private final List<Integer> tableOffsets;
    private final List<Integer> tableLengths;

    private final ExtractionManager extractionManager;
    private final ExtractionDialog extractionDialog;

    public GoogleExtractionThread_1_6_5(File font, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, ExtractionManager extractionManager, ExtractionDialog extractionDialog) {
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
            int cmapIndex = tableNames.indexOf("cmap");
            if (cmapIndex > -1) {
                int cmapOffset = tableOffsets.get(cmapIndex);
                int cmapLength = tableLengths.get(cmapIndex);

                inputStream.seek(cmapOffset);

                b = new byte[4];
                inputStream.readFully(b);

                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 1)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                b = new byte[4];
                inputStream.readFully(b);

                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x0A)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 2)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                inputStream.skipBytes(4);

                b = new byte[2];
                inputStream.readFully(b);

                if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x0C)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 3)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                inputStream.skipBytes(10);

                b = new byte[4];
                inputStream.readFully(b);

                int numGroupings = ExtractionUtilites.getIntFromBytes(b);
                HashMap<Integer, String> unicodeNameMap = new HashMap<>(); //Key: GlyphID, Value: Unicode Name String (i.e. uni00a9)

                //Iterate over each grouping and build unicode name table
                for (int i = 0; i < numGroupings; i++) {
                    inputStream.readFully(b);
                    int startCharCode = ExtractionUtilites.getIntFromBytes(b);
                    inputStream.readFully(b);
                    int endCharCode = ExtractionUtilites.getIntFromBytes(b);
                    inputStream.readFully(b);
                    int startGlyphID = ExtractionUtilites.getIntFromBytes(b);
                    for (int j = 0; j < (endCharCode - startCharCode) + 1; j++) {
                        String unicode = Integer.toHexString(startCharCode + j);
                        if (unicode.length() < 4)
                            for (int k = unicode.length(); k != 4; k++)
                                unicode = "0" + unicode;
                        if (unicode.length() == 6 && unicode.startsWith("0"))
                            unicode = unicode.substring(1);
                        unicode = "uni" + unicode;
                        appendToStatus("Added glyph name " + unicode + " for glyphID " + (startGlyphID + j));
                        unicodeNameMap.put(startGlyphID + j, unicode);
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

                    int GSUBIndex = tableNames.indexOf("GSUB");
                    if (GSUBIndex > -1) {
                        int GSUBOffset = tableOffsets.get(GSUBIndex);
                        int GSUBLength = tableLengths.get(GSUBIndex);

                        inputStream.seek(GSUBOffset);

                        b = new byte[4];
                        inputStream.readFully(b);
                        if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 1)");
                            inputStream.close();
                            return;
                        }

                        inputStream.skipBytes(4); //Skip ScriptList and FeatureList offsets

                        b = new byte[2];
                        inputStream.readFully(b); //Get offset of LookupList
                        int lookupListOffset = GSUBOffset + ExtractionUtilites.getShortFromBytes(b);
                        inputStream.seek(lookupListOffset); //Navigate to LookupList

                        System.out.println(GSUBOffset);
                        System.out.println(lookupListOffset);

                        inputStream.readFully(b); //Get LookupCount
                        if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x01)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 2)");
                            inputStream.close();
                            return;
                        }

                        inputStream.readFully(b); //Get first LookupTable Offset
                        int lookupTableOffset = lookupListOffset + ExtractionUtilites.getShortFromBytes(b);
                        inputStream.seek(lookupTableOffset); //Navigate to first LookupTable

                        b = new byte[6];
                        inputStream.readFully(b); //Get LookupType, LookupFlag, and SubTableCount.
                        if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 3)");
                            inputStream.close();
                            return;
                        }

                        b = new byte[2];
                        inputStream.readFully(b); //Get offset of Ligature Substitution Subtable
                        int ligatureTableOffset = lookupTableOffset + ExtractionUtilites.getShortFromBytes(b);
                        inputStream.seek(ligatureTableOffset); //Navigate to Ligature Substitution Subtable

                        b = new byte[2];
                        inputStream.readFully(b); //Get SubstFormat
                        if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x01)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 4)");
                            inputStream.close();
                            return;
                        }

                        inputStream.readFully(b); //Get Coverage Offset
                        int coverageOffset = ligatureTableOffset + ExtractionUtilites.getShortFromBytes(b);

                        inputStream.readFully(b); //Get LigSetCount
                        short ligSetCount = ExtractionUtilites.getShortFromBytes(b);

                        int[] ligSetOffsets = new int[ligSetCount];

                        for (int i = 0; i < ligSetCount; i++) {
                            inputStream.readFully(b);
                            ligSetOffsets[i] = ligatureTableOffset + ExtractionUtilites.getShortFromBytes(b);
                        }

                        /* Coverage Table */
                        inputStream.seek(coverageOffset);
                        inputStream.readFully(b);
                        if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x02)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 5)");
                            inputStream.close();
                            return;
                        }

                        inputStream.readFully(b);
                        short rangeCount = ExtractionUtilites.getShortFromBytes(b);
                        short[] coverageGlyphIDs = new short[ligSetCount]; //Array of Glyph IDs to be replaced, as specified by the coverage table
                        for (int i = 0; i < rangeCount; i++) {
                            inputStream.readFully(b);
                            short startGlyph = ExtractionUtilites.getShortFromBytes(b);
                            inputStream.readFully(b);
                            short endGlyph = ExtractionUtilites.getShortFromBytes(b);
                            inputStream.readFully(b);
                            short startCoverageIndex = ExtractionUtilites.getShortFromBytes(b);
                            for (int j = 0; j <= endGlyph - startGlyph; j++) {
                                coverageGlyphIDs[j + startCoverageIndex] = (short) (startGlyph + j);
                            }
                        }

                        /* LigSet Tables */
                        for (int i = 0; i < ligSetCount; i++) {
                            inputStream.seek(ligSetOffsets[i]);

                            inputStream.readFully(b);
                            short ligatureCount = ExtractionUtilites.getShortFromBytes(b);
                            int[] ligatureOffsets = new int[ligatureCount];
                            /* Ligature Tables */
                            for (int j = 0; j < ligatureCount; j++) {
                                inputStream.readFully(b);
                                ligatureOffsets[j] = ligSetOffsets[i] + ExtractionUtilites.getShortFromBytes(b);
                            }
                            for (int j = 0; j < ligatureCount; j++) {
                                inputStream.seek(ligatureOffsets[j]);
                                inputStream.readFully(b);
                                short ligGlyph = ExtractionUtilites.getShortFromBytes(b);
                                inputStream.readFully(b);
                                short compCount = ExtractionUtilites.getShortFromBytes(b);
                                StringBuilder stringBuilder = new StringBuilder(unicodeNameMap.get((int) coverageGlyphIDs[i]));
                                if (compCount > 1) {
                                    for (int k = 0; k < compCount - 1; k++) {
                                        inputStream.readFully(b);
                                        stringBuilder.append("_").append(unicodeNameMap.get((int) ExtractionUtilites.getShortFromBytes(b)));
                                    }
                                }
                                appendToStatus("Added substituted glyph name " + stringBuilder.toString() + " for glyphID " + ligGlyph);
                                unicodeNameMap.put((int) ligGlyph, stringBuilder.toString());
                            }
                        }
                    } else {
                        appendToStatus("Could not find 'GSUB' table! Continuing...");
                    }

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

                        System.out.println("# Emojis to Extract: " + (endGlyphID - beginGlyphID));
                        System.out.println("Begin: " + beginGlyphID + " - End: " + endGlyphID);

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
                                if (unicodeNameMap.get(i) != null) {
                                    inputStream.readFully(b);
                                    System.out.println("Extracting Emoji #" + i + " to '" + unicodeNameMap.get(i) + ".png'");
                                    appendToStatus("Extracting Emoji #" + i + " to '" + unicodeNameMap.get(i) + ".png'");
                                    FileOutputStream outputStream = new FileOutputStream(new File(extractionDirectory, unicodeNameMap.get(i) + ".png"));
                                    outputStream.write(b);
                                    outputStream.close();
                                } else
                                    inputStream.skipBytes(glyphLength);
                                updateProgress((int) ((i - beginGlyphID) / (float) (endGlyphID - beginGlyphID) * 100));
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
                extractionManager.showMessageDialog("Could not find 'cmap' table! Contact developer for help.");
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
