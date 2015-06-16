package me.MitchT.EmojiExtractor.Extractors;

import me.MitchT.EmojiExtractor.EmojiExtractor;
import me.MitchT.EmojiExtractor.ExtractionManager;
import me.MitchT.EmojiExtractor.GUI.ProgressPanel;

import java.io.*;

public class StandardExtractionThread extends ExtractionThread {
    private static final int[] prefix = new int[]{0x89, 0x50, 0x4E, 0x47};
    private static final int[] suffix = new int[]{0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82};
    private static final File emojisDir = new File(EmojiExtractor.getRootDirectory() + "/ExtractedEmojis");
    private static boolean[] searchBooleans = new boolean[8];
    private long currentBytePos = 0;
    private ExtractionManager extractionManager;
    private ProgressPanel progressPanel;

    private long startTime = 0;
    private long currTime = 0;

    public StandardExtractionThread(File font, ExtractionManager extractionManager, ProgressPanel progressPanel) {
        super(font);
        this.extractionManager = extractionManager;
        this.progressPanel = progressPanel;

        progressPanel.setShowTimeRemaining(true);
        progressPanel.setShowStatusMessage(true);
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = new FileInputStream(this.font);

            if (!emojisDir.exists()) {
                emojisDir.mkdir();
            }

            startTime = System.currentTimeMillis();

            int imageID = 0;
            while (inputStream.available() >= 1) {
                if (!running) {
                    inputStream.close();
                    return;
                }

                if (progressPanel.getStopped())
                    continue;

                if (currentBytePos % 512 == 0) {
                    setProgressStatusMessage("Searching for Emojis - Please wait until complete!");
                    updateProgress();
                }

                if (checkForPrefix(inputStream.read())) {
                    imageID++;
                    extractEmoji(inputStream, imageID);
                    updateProgress();
                }
            }
            inputStream.close();

            System.out.println("No more Emojis to extract! All done! :)");
            extractionManager.showMessagePanel("No more Emojis to extract! All done! :)");
        } catch (FileNotFoundException e) {
            System.out.println(this.font.getName() + " not found!");
            extractionManager.showMessagePanel(this.font.getName() + " not found!");
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
        setProgressStatusMessage("Extracting Emoji #" + emojiID + " to '" + emojiID + ".png'");
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(emojisDir, emojiID + ".png"));

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
        this.currTime = System.currentTimeMillis();
        progressPanel.setProgress((int) (((double) currentBytePos / this.font.length()) * 100));
        progressPanel.setTimeRemaining(currentBytePos, this.font.length(), currTime, startTime);
    }

    private void setProgressStatusMessage(String message) {
        progressPanel.setStatusMessage(message);
    }

}
