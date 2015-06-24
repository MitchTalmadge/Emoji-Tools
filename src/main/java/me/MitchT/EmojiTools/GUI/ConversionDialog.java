package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import java.awt.*;

public class ConversionDialog extends ProgressDialog {

    public ConversionDialog(OperationTab gui, Image logo) {
        super(gui, "Converting Emojis", logo);
    }

}
