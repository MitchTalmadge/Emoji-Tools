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

package net.liveforcode.EmojiTools2.Extraction;

import net.liveforcode.EmojiTools2.EmojiTools;
import net.liveforcode.EmojiTools2.Extraction.Extractors.AppleExtractionThread;
import net.liveforcode.EmojiTools2.Extraction.Extractors.ExtractionThread;
import net.liveforcode.EmojiTools2.Extraction.Extractors.GoogleExtractionThread;
import net.liveforcode.EmojiTools2.OldGUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools2.OldGUI.ExtractionDialog;
import net.liveforcode.EmojiTools2.JythonHandler;
import net.liveforcode.EmojiTools2.OperationManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class ExtractionManager extends OperationManager implements EmojiTools.JythonListener {

    private final File font;
    private final File extractionDirectory;
    private final EmojiToolsGUI gui;
    private final ExtractionDialog extractionDialog;
    private List<String> tableNames;
    private List<Integer> tableOffsets;
    private List<Integer> tableLengths;
    private ExtractionThread extractionThread;

    public ExtractionManager(File font, File extractionDirectory, EmojiToolsGUI gui, ExtractionDialog extractionDialog) {
        this.font = font;
        this.extractionDirectory = extractionDirectory;
        this.gui = gui;
        this.extractionDialog = extractionDialog;

        //Determine which Extraction Method to use
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            if (!ExtractionUtilites.compareBytes(inputStream, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                showMessageDialog("Selected Font is not a valid True Type Font! (*.ttf)");
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
            EmojiTools.submitError(Thread.currentThread(), e);
        }
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }

    @Override
    public void start() {
        EmojiTools.addJythonListener(this);
    }

    @Override
    public void onJythonReady(JythonHandler jythonHandler) {
        if (tableNames.contains("sbix"))
            extractionThread = new AppleExtractionThread(font, extractionDirectory, tableNames, tableOffsets, tableLengths, this, extractionDialog, jythonHandler);
        else if (tableNames.contains("CBLC") && tableNames.contains("CBDT"))
            extractionThread = new GoogleExtractionThread(font, extractionDirectory, tableNames, tableOffsets, tableLengths, this, extractionDialog, jythonHandler);
        else {
            gui.showMessageDialog("The selected font cannot be extracted. Contact developer for help.");
            return;
        }
        this.gui.getConsoleManager().addConsoleListener(extractionThread);
        extractionThread.start();
    }

    @Override
    public void stop() {
        if (extractionThread != null && extractionThread.isAlive()) {
            extractionThread.endExtraction();
        }
    }

    public enum TTXType {
        ANDROID("android.ttx"),
        IOS("ios.ttx"),
        OSX("osx.ttx");

        private String fileName;

        TTXType(String fileName) {

            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
