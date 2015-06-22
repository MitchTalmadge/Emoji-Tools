package me.MitchT.EmojiTools.Extraction.Extractors;

import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.GUI.ExtractionDialog;

import java.io.*;

public class StandardExtractionThread extends ExtractionThread {
    private static final int[] prefix = new int[]{0x89, 0x50, 0x4E, 0x47};
    private static final int[] suffix = new int[]{0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82};
    private static boolean[] searchBooleans = new boolean[8];
    private final ExtractionManager extractionManager;
    private final ExtractionDialog extractionDialog;
    private long currentBytePos = 0;

    public StandardExtractionThread(File font, String exportDirectoryName, ExtractionManager extractionManager, ExtractionDialog extractionDialog) {
        super(font, exportDirectoryName);
        this.extractionManager = extractionManager;
        this.extractionDialog = extractionDialog;

    }

    @Override
    public void run() {
        try {
            InputStream inputStream = new FileInputStream(this.font);

            if (!exportDir.exists()) {
                exportDir.mkdir();
            }

            appendToStatus("Searching for Emojis - Please wait until complete!");

            int imageID = 0;
            while (inputStream.available() >= 1) {
                if (!running) {
                    inputStream.close();
                    extractionDialog.dispose();
                    return;
                }

                if (currentBytePos % 512 == 0) {
                    updateProgress();
                }

                if (checkForPrefix(inputStream.read())) {
                    imageID++;
                    extractEmoji(inputStream, imageID);
                    updateProgress();
                }
            }
            inputStream.close();

            extractionDialog.dispose();
        } catch (FileNotFoundException e) {
            System.out.println(this.font.getName() + " not found!");
            extractionManager.showMessageDialog(this.font.getName() + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the next emoji from the inputStream into a '.png' file inside of <i>emojisDir</i>.
     *
     * @param inputStream The inputStream to read from.
     * @param emojiID     The ID of the emoji (used for naming conventions).
     */
    private void extractEmoji(InputStream inputStream, int emojiID) {
        resetSearchBooleans();

        System.out.println("Extracting Emoji #" + emojiID + " to '" + emojiID + ".png'");
        appendToStatus("Extracting Emoji #" + emojiID + " to '" + emojiID + ".png'");
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(exportDir, emojiID + ".png"));

            for (int num : prefix)
                outputStream.write(num);

            while (inputStream.available() >= 1) {
                int b = inputStream.read();
                currentBytePos++;
                outputStream.write(b);
                if (checkForSuffix(b))
                    break;
            }

            outputStream.close();

            resetSearchBooleans();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks bytes being read to see if they match the defined prefix. Ensures order and accuracy.
     *
     * @param val Byte to check.
     * @return True if all bytes up to this one match all bytes in the defined prefix.<br>
     * False if the entire prefix has not been matched yet, or one of the bytes did not match.
     */
    private boolean checkForPrefix(int val) {
        currentBytePos++;
        for (int i = 0; i < prefix.length; i++) {
            if (!searchBooleans[i])
                if (prefix[i] == val) {
                    searchBooleans[i] = true;
                    break;
                } else {
                    resetSearchBooleans();
                    break;
                }
        }
        return searchBooleans[prefix.length - 1];
    }

    /**
     * Checks bytes being read to see if they match the defined suffix. Ensures order and accuracy.
     *
     * @param val Byte to check.
     * @return True if all bytes up to this one match all bytes in the defined suffix.<br>
     * False if the entire suffix has not been matched yet, or one of the bytes did not match.
     */
    private boolean checkForSuffix(int val) {
        for (int i = 0; i < suffix.length; i++) {
            if (!searchBooleans[i])
                if (suffix[i] == val) {
                    searchBooleans[i] = true;
                    break;
                } else {
                    resetSearchBooleans();
                    break;
                }
        }
        return searchBooleans[suffix.length - 1];
    }

    /**
     * Must be called at the beginning of checking bytes against the prefix or suffix. Clears all previously checked bytes.
     */
    private void resetSearchBooleans() {
        searchBooleans = new boolean[8];
    }

    private void updateProgress() {
        extractionDialog.setProgress((int) (((double) currentBytePos / this.font.length()) * 100));
    }

    private void appendToStatus(String message) {
        extractionDialog.appendToStatus(message);
    }

}
