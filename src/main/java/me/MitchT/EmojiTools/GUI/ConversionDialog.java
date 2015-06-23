package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import java.awt.*;

public class ConversionDialog extends ProgressDialog {

    public ConversionDialog(ExtractionTab gui, Image logo) {
        super(gui, "Converting Emojis", logo);
    }

}
