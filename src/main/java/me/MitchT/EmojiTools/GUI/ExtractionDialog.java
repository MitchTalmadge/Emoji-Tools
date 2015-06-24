package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import java.awt.*;

public class ExtractionDialog extends ProgressDialog {

    public ExtractionDialog(OperationTab gui, Image logo) {
        super(gui, "Extracting Emojis", logo);
    }

}
