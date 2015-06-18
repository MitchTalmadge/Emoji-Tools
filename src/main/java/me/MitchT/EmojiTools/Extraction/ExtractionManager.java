package me.MitchT.EmojiTools.Extraction;

import me.MitchT.EmojiTools.Extraction.Extractors.AppleExtractionThread;
import me.MitchT.EmojiTools.Extraction.Extractors.ExtractionThread;
import me.MitchT.EmojiTools.Extraction.Extractors.GoogleExtractionThread;
import me.MitchT.EmojiTools.Extraction.Extractors.StandardExtractionThread;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.ExtractionDialog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class ExtractionManager {

    private EmojiToolsGUI gui;

    private ExtractionThread extractionThread;


    public ExtractionManager(File font, String exportDirectoryName, EmojiToolsGUI gui, ExtractionDialog extractionDialog) {
        this.gui = gui;

        //Determine which Extraction Method to use
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            byte[] b = new byte[4];
            inputStream.readFully(b);

            if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                showMessageDialog("Selected Font is not a valid True Type Font! (*.ttf)");
                inputStream.close();
                return;
            }

            b = new byte[2];
            inputStream.readFully(b);

            short numTables = ExtractionUtilites.getShortFromBytes(b);
            List<String> tableNames = Arrays.asList(new String[numTables]);
            List<Integer> tableOffsets = Arrays.asList(new Integer[numTables]);
            List<Integer> tableLengths = Arrays.asList(new Integer[numTables]);

            inputStream.seek(12);
            b = new byte[4];
            for (int i = 0; i < numTables; i++) {
                inputStream.readFully(b);
                tableNames.set(i, ExtractionUtilites.getStringFromBytes(b));
                inputStream.skipBytes(4);
                inputStream.readFully(b);
                tableOffsets.set(i, ExtractionUtilites.getIntFromBytes(b));
                inputStream.readFully(b);
                tableLengths.set(i, ExtractionUtilites.getIntFromBytes(b));
            }

            if (tableNames.contains("sbix"))
                extractionThread = new AppleExtractionThread(font, exportDirectoryName, tableNames, tableOffsets, tableLengths, this, extractionDialog);
            else if (tableNames.contains("CBLC") && tableNames.contains("CBDT"))
                extractionThread = new GoogleExtractionThread(font, exportDirectoryName, tableNames, tableOffsets, tableLengths, this, extractionDialog);
            else
                extractionThread = new StandardExtractionThread(font, exportDirectoryName, this, extractionDialog);

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startExtraction() {
        extractionThread.start();
    }

    public void stopExtraction() {
        if (extractionThread != null && extractionThread.isAlive()) {
            extractionThread.endExtraction();
        }
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }
}
