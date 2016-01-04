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

package net.liveforcode.emojitools.operations.extraction.extractors;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.extraction.ExtractionOperation;
import net.liveforcode.emojitools.operations.extraction.ExtractionUtilites;
import org.python.core.PyList;
import org.python.core.PyType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleExtractionWorker extends ExtractionWorker {

    public GoogleExtractionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File fontFile, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths) {
        super(operation, operationProgressDialog, fontFile, extractionDirectory, tableNames, tableOffsets, tableLengths, true);
    }

    @Override
    protected Boolean doWork() throws Exception {
        //---- ttx.py ----//
        setProgressIndeterminate();
        appendMessageToDialog("Converting Emoji Font... Please wait...");

        //Set sys.argv
        ArrayList<String> argvList = new ArrayList<>();
        argvList.add("package.py");                                         //Python Script Name
        argvList.add("-o");                                                 //Output flag
        argvList.add(extractionDirectory.getAbsolutePath()
                + "/" + ExtractionOperation.TTXType.ANDROID.getFileName());   //Output ttx path
        argvList.add(fontFile.getAbsolutePath());                               //Input ttf path

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        if (isCancelled())
            return false;

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath()
                + "/PythonScripts/package.py");

        try {
            RandomAccessFile inputStream = new RandomAccessFile(fontFile, "r");

            appendMessageToDialog("Searching for Emojis - Please wait until complete!");

            //Get numGlyphs, ordinal numbers, and glyphNames from post table
            int cmapIndex = tableNames.indexOf("cmap");
            if (cmapIndex > -1) {
                int cmapOffset = tableOffsets.get(cmapIndex);
                int cmapLength = tableLengths.get(cmapIndex);

                inputStream.seek(cmapOffset);

                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x00)) {
                    //extractionOperationManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 1)");
                    inputStream.close();
                    return false;
                }

                short numSubTables = inputStream.readShort();
                short[] platformIDs = new short[numSubTables];
                short[] platformSpecificIDs = new short[numSubTables];
                int[] subTableOffsets = new int[numSubTables];

                //Key: GlyphID, Value: Unicode Name String (i.e. uni00a9)
                HashMap<Integer, String> unicodeNameMap = new HashMap<>();

                for (short subTableId = 0; subTableId < numSubTables; subTableId++) {
                    platformIDs[subTableId] = inputStream.readShort();
                    platformSpecificIDs[subTableId] = inputStream.readShort();
                    subTableOffsets[subTableId] = inputStream.readInt();
                }

                for (int subTableId = 0; subTableId < numSubTables; subTableId++) {

                    //Go to beginning of subTable
                    inputStream.seek(cmapOffset + subTableOffsets[subTableId]);

                    if (platformIDs[subTableId] == 3 && platformSpecificIDs[subTableId] == 10) {
                        //Platform ID = Microsoft, Platform Specific ID = Unicode UCS-4

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x0C)) {
                            //extractionOperationManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 3:1)");
                            inputStream.close();
                            return false;
                        }

                        inputStream.skipBytes(10); //Skip Reserved, Length, and Language

                        int numGroupings = inputStream.readInt();

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
                                appendMessageToDialog("Added glyph name " + unicode + " for glyphID " + (startGlyphID + j));
                                unicodeNameMap.put(startGlyphID + j, unicode);
                            }
                        }

                        int CBLCIndex = tableNames.indexOf("CBLC");
                        if (CBLCIndex > -1) {
                            int CBLCOffset = tableOffsets.get(CBLCIndex);
                            int CBLCLength = tableLengths.get(CBLCIndex);

                            inputStream.seek(CBLCOffset);

                            if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                                //extractionOperationManager.showMessageDialog("Invalid 'CBLC' table! Contact developer for help.");
                                inputStream.close();
                                return false;
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
                                    //extractionOperationManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 1)");
                                    inputStream.close();
                                    return false;
                                }

                                inputStream.skipBytes(4); //Skip ScriptList and FeatureList offsets

                                int lookupListOffset = GSUBOffset + inputStream.readShort(); //Get offset of LookupList
                                inputStream.seek(lookupListOffset); //Navigate to LookupList

                                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01)) { //Get LookupCount
                                    //extractionOperationManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 2)");
                                    inputStream.close();
                                    return false;
                                }

                                int lookupTableOffset = lookupListOffset + inputStream.readShort(); //Get first LookupTable Offset
                                inputStream.seek(lookupTableOffset); //Navigate to first LookupTable

                                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01)) { //Get LookupType, LookupFlag, and SubTableCount.
                                    //extractionOperationManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 3)");
                                    inputStream.close();
                                    return false;
                                }

                                int ligatureTableOffset = lookupTableOffset + inputStream.readShort(); //Get offset of Ligature Substitution Subtable
                                inputStream.seek(ligatureTableOffset); //Navigate to Ligature Substitution Subtable

                                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01)) { //Get SubstFormat
                                    //extractionOperationManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 4)");
                                    inputStream.close();
                                    return false;
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
                                    //extractionOperationManager.showMessageDialog("Invalid 'GSUB' table! Contact developer for help. (Code 5)");
                                    inputStream.close();
                                    return false;
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
                                        appendMessageToDialog("Added substituted glyph name " + stringBuilder.toString() + " for glyphID " + ligGlyph);
                                        unicodeNameMap.put((int) ligGlyph, stringBuilder.toString());
                                    }
                                }
                            } else {
                                appendMessageToDialog("Could not find 'GSUB' table! Continuing...");
                            }

                            //Get number of strikes, and scan for PNG files.
                            int CBDTIndex = tableNames.indexOf("CBDT");
                            if (CBDTIndex > -1) {
                                int CBDTOffset = tableOffsets.get(CBDTIndex);
                                int CBDTLength = tableLengths.get(CBDTIndex);

                                inputStream.seek(CBDTOffset);

                                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                                    //extractionOperationManager.showMessageDialog("Invalid 'CBDT' table! Contact developer for help.");
                                    inputStream.close();
                                    return false;
                                }

                                System.out.println("# Emojis to Extract: " + (endGlyphID - beginGlyphID));
                                System.out.println("Begin: " + beginGlyphID + " - End: " + endGlyphID);

                                for (int i = beginGlyphID; i <= endGlyphID; i++) {
                                    if (isCancelled()) {
                                        inputStream.close();
                                        return false;
                                    }
                                    inputStream.skipBytes(5);

                                    int glyphLength = inputStream.readInt();
                                    if (glyphLength > 0) {
                                        byte[] b = new byte[glyphLength];
                                        if (unicodeNameMap.get(i) != null) {
                                            appendMessageToDialog("Extracting Emoji #" + i + " to '" + unicodeNameMap.get(i) + ".png'");
                                            FileOutputStream outputStream = new FileOutputStream(new File(extractionDirectory, unicodeNameMap.get(i) + ".png"));
                                            inputStream.readFully(b);
                                            outputStream.write(b);
                                            outputStream.close();
                                        } else
                                            inputStream.skipBytes(glyphLength);
                                        updateProgress(i - beginGlyphID, endGlyphID - beginGlyphID);
                                    }
                                }

                            } else {
                                //extractionOperationManager.showMessageDialog("Could not find 'CBDT' table! Contact developer for help.");
                                inputStream.close();
                                return false;
                            }
                        } else {
                            //extractionOperationManager.showMessageDialog("Could not find 'CBLC' table! Contact developer for help.");
                            inputStream.close();
                            return false;
                        }
                    } else if (platformIDs[subTableId] == 0 && platformSpecificIDs[subTableId] == 5) {
                        //Platform ID = Unicode, //Platform Specific ID = Unicode Variation Sequences

                        if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x0E)) {
                            //extractionOperationManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 3:2)");
                            inputStream.close();
                            return false;
                        }

                        //TODO: Format 14
                    } else {
                        //extractionOperationManager.showMessageDialog("Invalid 'cmap' table format! Contact developer for help. (Code 2:" + subTableId + ")");
                        inputStream.close();
                        return false;
                    }
                }
            } else {
                //extractionOperationManager.showMessageDialog("Could not find 'cmap' table! Contact developer for help.");
                inputStream.close();
                return false;
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println(this.fontFile.getName() + " not found!");
            //extractionOperationManager.showMessageDialog(this.fontFile.getName() + " not found!");
        } catch (IOException e) {
            EmojiTools.submitError(Thread.currentThread(), e);
        }
        return true;
    }
}
