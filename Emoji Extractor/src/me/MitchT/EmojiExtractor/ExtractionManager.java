package me.MitchT.EmojiExtractor;

import me.MitchT.EmojiExtractor.Extractors.AppleExtractionThread;
import me.MitchT.EmojiExtractor.Extractors.ExtractionThread;
import me.MitchT.EmojiExtractor.Extractors.StandardExtractionThread;
import me.MitchT.EmojiExtractor.GUI.MainFrame;
import me.MitchT.EmojiExtractor.GUI.ProgressPanel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class ExtractionManager {

    private File font;
    private MainFrame mainFrame;

    private ExtractionThread extractionThread;


    public ExtractionManager(File font, MainFrame mainFrame, ProgressPanel progressPanel) {
        this.font = font;
        this.mainFrame = mainFrame;

        //Determine which Extraction Method to use
        try {
            RandomAccessFile inputStream = new RandomAccessFile(font, "r");

            byte[] b = new byte[4];
            inputStream.readFully(b);

            if (!ExtractionUtilites.compareBytes(b, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00)) {
                showMessagePanel("Selected Font is not a valid True Type Font! (*.ttf)");
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
                extractionThread = new AppleExtractionThread(font, tableNames, tableOffsets, tableLengths, this, progressPanel);
            else
                extractionThread = new StandardExtractionThread(font, this, progressPanel);

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

    public void showMessagePanel(String message) {
        this.mainFrame.showMessagePanel(message);
    }
}
