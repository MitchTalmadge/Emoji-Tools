package me.MitchT.EmojiTools.Conversion;

import me.MitchT.EmojiTools.GUI.ConversionDialog;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.OperationManager;

import java.io.File;

public class ConversionManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final ConversionThread conversionThread;


    public ConversionManager(File conversionFile, EmojiToolsGUI gui, ConversionDialog conversionDialog, boolean CgBItoRGBA) {
        this.gui = gui;

        this.conversionThread = new ConversionThread(conversionFile, this, conversionDialog, CgBItoRGBA);
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }

    @Override
    public void start() {
        conversionThread.start();
    }

    @Override
    public void stop() {
        if (conversionThread != null && conversionThread.isAlive()) {
            conversionThread.endConversion();
        }
    }
}
