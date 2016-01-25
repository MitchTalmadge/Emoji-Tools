/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
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
import net.liveforcode.emojitools.operations.FontType;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.extraction.ExtractionUtilites;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class AppleExtractionWorker extends ExtractionWorker {

    public AppleExtractionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File fontFile, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths) {
        super(operation, operationProgressDialog, fontFile, extractionDirectory, tableNames, tableOffsets, tableLengths, false);
    }

    @Override
    protected Boolean doWork() throws Exception {
        try {
            RandomAccessFile inputStream = new RandomAccessFile(fontFile, "r");

            appendMessageToDialog("Searching for Emojis - Please wait until complete!");

            //Get numGlyphs, ordinal numbers, and glyphNames from post table
            int postIndex = tableNames.indexOf("post");
            if (postIndex > -1) {
                int postOffset = tableOffsets.get(postIndex);
                int postLength = tableLengths.get(postIndex);

                inputStream.seek(postOffset);


                if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00)) {
                    showErrorDialog("Invalid 'post' Table", "The font's 'post' table is an invalid format. Most likely, support for this font has not been added yet. Please contact the developer for help.");
                    inputStream.close();
                    return false;
                }

                inputStream.skipBytes(28);

                short numGlyphs = inputStream.readShort();
                short[] ordinalNumbers = new short[numGlyphs];

                short numNewGlyphs = 0;

                for (int i = 0; i < numGlyphs; i++) {
                    ordinalNumbers[i] = inputStream.readShort();
                    if (ordinalNumbers[i] > 257)
                        numNewGlyphs++;
                }

                String[] extraNames = new String[numNewGlyphs];

                for (int i = 0; i < numNewGlyphs; i++) {

                    short nameLen = (short) inputStream.read();
                    extraNames[i] = ExtractionUtilites.getByteString(inputStream, nameLen);
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

                    int numStrikes = inputStream.readInt();
                    int[] strikeOffsets = new int[numStrikes];
                    for (int i = 0; i < numStrikes; i++) {
                        strikeOffsets[i] = inputStream.readInt();
                    }

                    short[] resolutions = new short[numStrikes];
                    for(int i = 0; i < numStrikes; i++)
                    {
                        inputStream.seek(sbixOffset + strikeOffsets[i]);
                        resolutions[i] = inputStream.readShort();
                    }

                    inputStream.seek(sbixOffset + strikeOffsets[strikeOffsets.length - 1]);
                    inputStream.skipBytes(4);

                    int[] glyphOffsets = new int[numGlyphs];
                    int[] glyphLengths = new int[numGlyphs];

                    for (int i = 0; i < numGlyphs; i++) {
                        glyphOffsets[i] = inputStream.readInt();
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
                        if (isCancelled()) {
                            inputStream.close();
                            return false;
                        }
                        updateProgress(i, numGlyphs);

                        if (glyphLengths[i] > 0) {
                            inputStream.skipBytes(4);
                            if (ExtractionUtilites.getByteString(inputStream, 4).equals("png ")) {
                                appendMessageToDialog("Extracting Emoji #" + i + " to '" + glyphNames[i] + ".png'");
                                FileOutputStream outputStream = new FileOutputStream(new File(extractionDirectory, glyphNames[i] + ".png"));
                                byte[] b = new byte[glyphLengths[i] - 8];
                                inputStream.readFully(b);
                                outputStream.write(b);
                                outputStream.close();
                            }
                        }
                    }

                    Files.copy(fontFile.toPath(), new File(extractionDirectory, "Original.ttf").toPath());
                    writeFontTypeFile(FontType.APPLE, resolutions);
                } else {
                    showErrorDialog("Missing 'sbix' Table", "The font's 'sbix' table is missing. Most likely, support for this font has not been added yet. Please contact the developer for help.");
                    inputStream.close();
                    return false;
                }
            } else {
                showErrorDialog("Missing 'post' Table", "The font's 'post' table is missing. Most likely, support for this font has not been added yet. Please contact the developer for help.");
                inputStream.close();
                return false;
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            showErrorDialog("Unable to Locate Font", "The chosen font could not be found. Did it get deleted?");
        } catch (IOException e) {
            EmojiTools.submitError(e);
        }
        return true;
    }

}
