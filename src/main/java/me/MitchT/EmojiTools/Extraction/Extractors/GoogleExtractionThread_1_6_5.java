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

                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 1)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x0A)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 2)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                inputStream.skipBytes(4);

                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x0C)) {
                    extractionManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 3)");
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                inputStream.skipBytes(10);

                int numGroupings = inputStream.readInt();
                HashMap<Integer, String> unicodeNameMap = new HashMap<>(); //Key: GlyphID, Value: Unicode Name String (i.e. uni00a9)

                //Iterate over each grouping and build unicode name table
                for (int i = 0; i < numGroupings; i++) {
                    int startCharCode = inputStream.readInt();
                    int endCharCode = inputStream.readInt();
                    int startGlyphID = inputStream.readInt();
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

                    if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                        extractionManager.showMessageDialog("Invalid 'CBLC' table! Contact developer for help.");
                        inputStream.close();
                        return;
                    }

                    inputStream.skipBytes(44);

                    short beginGlyphID = inputStream.readShort();

                    short endGlyphID = inputStream.readShort();

                    int GSUBIndex = tableNames.indexOf("GSUB");
                    if (GSUBIndex > -1) {
                        int GSUBOffset = tableOffsets.get(GSUBIndex);
                        int GSUBLength = tableLengths.get(GSUBIndex);

                        inputStream.seek(GSUBOffset);

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 1)");
                            inputStream.close();
                            return;
                        }

                        inputStream.skipBytes(4); //Skip ScriptList and FeatureList offsets

                        int lookupListOffset = GSUBOffset + inputStream.readShort(); //Get offset of LookupList
                        inputStream.seek(lookupListOffset); //Navigate to LookupList

                        System.out.println(GSUBOffset);
                        System.out.println(lookupListOffset);

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01)) { //Get LookupCount
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 2)");
                            inputStream.close();
                            return;
                        }

                        int lookupTableOffset = lookupListOffset + inputStream.readShort(); //Get first LookupTable Offset
                        inputStream.seek(lookupTableOffset); //Navigate to first LookupTable

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01)) { //Get LookupType, LookupFlag, and SubTableCount.
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 3)");
                            inputStream.close();
                            return;
                        }

                        int ligatureTableOffset = lookupTableOffset + inputStream.readShort(); //Get offset of Ligature Substitution Subtable
                        inputStream.seek(ligatureTableOffset); //Navigate to Ligature Substitution Subtable

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01)) { //Get SubstFormat
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 4)");
                            inputStream.close();
                            return;
                        }

                        int coverageOffset = ligatureTableOffset + inputStream.readShort(); //Get Coverage Offset

                        short ligSetCount = inputStream.readShort(); //Get LigSetCount

                        int[] ligSetOffsets = new int[ligSetCount];

                        for (int i = 0; i < ligSetCount; i++) {
                            ligSetOffsets[i] = ligatureTableOffset + inputStream.readShort();
                        }

                        /* Coverage Table */
                        inputStream.seek(coverageOffset);
                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02)) {
                            extractionManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 5)");
                            inputStream.close();
                            return;
                        }

                        short rangeCount = inputStream.readShort();
                        short[] coverageGlyphIDs = new short[ligSetCount]; //Array of Glyph IDs to be replaced, as specified by the coverage table
                        for (int i = 0; i < rangeCount; i++) {
                            short startGlyph = inputStream.readShort();
                            short endGlyph = inputStream.readShort();
                            short startCoverageIndex = inputStream.readShort();
                            for (int j = 0; j <= endGlyph - startGlyph; j++) {
                                coverageGlyphIDs[j + startCoverageIndex] = (short) (startGlyph + j);
                            }
                        }

                        /* LigSet Tables */
                        for (int i = 0; i < ligSetCount; i++) {
                            inputStream.seek(ligSetOffsets[i]);

                            short ligatureCount = inputStream.readShort();
                            int[] ligatureOffsets = new int[ligatureCount];
                            /* Ligature Tables */
                            for (int j = 0; j < ligatureCount; j++) {
                                ligatureOffsets[j] = ligSetOffsets[i] + inputStream.readShort();
                            }
                            for (int j = 0; j < ligatureCount; j++) {
                                inputStream.seek(ligatureOffsets[j]);
                                short ligGlyph = inputStream.readShort();
                                short compCount = inputStream.readShort();
                                StringBuilder stringBuilder = new StringBuilder(unicodeNameMap.get((int) coverageGlyphIDs[i]));
                                if (compCount > 1) {
                                    for (int k = 0; k < compCount - 1; k++) {
                                        stringBuilder.append("_").append(unicodeNameMap.get((int) inputStream.readShort()));
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

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
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

                            int glyphLength = inputStream.readInt();
                            if (glyphLength > 0) {
                                byte[] b = new byte[glyphLength];
                                if (unicodeNameMap.get(i) != null) {
                                    System.out.println("Extracting Emoji #" + i + " to '" + unicodeNameMap.get(i) + ".png'");
                                    appendToStatus("Extracting Emoji #" + i + " to '" + unicodeNameMap.get(i) + ".png'");
                                    FileOutputStream outputStream = new FileOutputStream(new File(extractionDirectory, unicodeNameMap.get(i) + ".png"));
                                    inputStream.readFully(b);
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
