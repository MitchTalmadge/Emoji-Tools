package me.MitchT.EmojiTools.Conversion;

import me.MitchT.EmojiTools.GUI.ConversionDialog;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;

import java.io.File;

public class ConversionManager {

    private EmojiToolsGUI gui;

    private ConversionThread conversionThread;


    public ConversionManager(File conversionFile, EmojiToolsGUI gui, ConversionDialog conversionDialog) {
        this.gui = gui;

    }

    public void startConversion() {
        conversionThread.start();
    }

    public void stopConversion() {
        if (conversionThread != null && conversionThread.isAlive()) {
            conversionThread.endConversion();
        }
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }
}
