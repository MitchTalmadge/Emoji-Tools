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

package net.liveforcode.emojitools.operations.extraction;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;
import net.liveforcode.emojitools.operations.extraction.extractors.AppleExtractionWorker;
import net.liveforcode.emojitools.operations.extraction.extractors.GoogleExtractionWorker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class ExtractionOperation extends Operation {

    private final File fontFile;
    private final File extractionDirectory;
    private List<String> tableNames;
    private List<Integer> tableOffsets;
    private List<Integer> tableLengths;

    public ExtractionOperation(File fontFile, File extractionDirectory) {
        this.fontFile = fontFile;
        this.extractionDirectory = extractionDirectory;

        //Determine which Extraction Method to use
        try {
            RandomAccessFile inputStream = new RandomAccessFile(fontFile, "r");

            if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                //showMessageDialog("Selected Font is not a valid True Type Font! (*.ttf)");
                inputStream.close();
                return;
            }

            short numTables = inputStream.readShort();
            tableNames = Arrays.asList(new String[numTables]);
            tableOffsets = Arrays.asList(new Integer[numTables]);
            tableLengths = Arrays.asList(new Integer[numTables]);

            inputStream.seek(12);
            for (int i = 0; i < numTables; i++) {
                tableNames.set(i, ExtractionUtilites.getByteString(inputStream, 4));
                inputStream.skipBytes(4);
                tableOffsets.set(i, inputStream.readInt());
                tableLengths.set(i, inputStream.readInt());
            }

            inputStream.close();
        } catch (IOException e) {
            EmojiTools.submitError(e);
        }
    }

    @Override
    protected OperationWorker getWorker() {
        if (tableNames.contains("sbix"))
            return new AppleExtractionWorker(this, new OperationProgressDialog("Extracting Apple Emojis..."), fontFile, extractionDirectory, tableNames, tableOffsets, tableLengths);
        else if (tableNames.contains("CBLC") && tableNames.contains("CBDT"))
            return new GoogleExtractionWorker(this, new OperationProgressDialog("Extracting Android Emojis..."), fontFile, extractionDirectory, tableNames, tableOffsets, tableLengths);
        else {
            //gui.showMessageDialog("The selected font cannot be extracted. Contact developer for help.");
            return null;
        }
    }
}
