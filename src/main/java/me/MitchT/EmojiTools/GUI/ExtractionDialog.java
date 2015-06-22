package me.MitchT.EmojiTools.GUI;

import java.awt.*;

public class ExtractionDialog extends ProgressDialog {

    public ExtractionDialog(EmojiToolsGUI gui, Image logo) {
        super(gui, "Extracting Emojis", logo);
    }

}
