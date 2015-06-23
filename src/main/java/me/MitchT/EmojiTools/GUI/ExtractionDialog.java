package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import java.awt.*;

public class ExtractionDialog extends ProgressDialog {

    public ExtractionDialog(ExtractionTab gui, Image logo) {
        super(gui, "Extracting Emojis", logo);
    }

}
