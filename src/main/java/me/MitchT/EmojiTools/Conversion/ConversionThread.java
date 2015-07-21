package me.MitchT.EmojiTools.Conversion;

import me.MitchT.EmojiTools.Conversion.Converter.Converter;
import me.MitchT.EmojiTools.GUI.ConversionDialog;

import java.io.File;

class ConversionThread extends Thread {

    private final File conversionFile;
    private final ConversionManager conversionManager;
    private final ConversionDialog conversionDialog;
    private final boolean CgBItoRGBA;
    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public ConversionThread(File conversionFile, ConversionManager conversionManager, ConversionDialog conversionDialog, boolean CgBItoRGBA) {
        super("ConversionThread");
        this.conversionFile = conversionFile;
        this.conversionManager = conversionManager;
        this.conversionDialog = conversionDialog;
        this.CgBItoRGBA = CgBItoRGBA;
    }

    @Override
    public void run() {

        File[] files;

        if (conversionFile.isDirectory()) {
            files = conversionFile.listFiles();
        } else {
            files = new File[]{conversionFile};
        }

        totalFileNum = files.length;

        Converter converter = new Converter();

        for (File file : files) {
            if (!running) {
                conversionDialog.dispose();
                return;
            }

            updateProgress();

            currentFileNum++;

            if (file.getName().endsWith(".png")) {
                System.out.println("Converting " + file.getName());
                conversionDialog.appendToStatus("Converting " + file.getName());

                converter.convertFile(file, CgBItoRGBA);
            }
        }

        updateProgress();

        conversionDialog.dispose();
    }

    private void updateProgress() {
        conversionDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endConversion() {
        running = false;
    }
}
