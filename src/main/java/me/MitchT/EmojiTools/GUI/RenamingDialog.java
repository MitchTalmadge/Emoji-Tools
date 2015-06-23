package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import java.awt.*;

public class RenamingDialog extends ProgressDialog {

    public RenamingDialog(ExtractionTab gui, Image logo) {
        super(gui, "Renaming Emojis", logo);
    }

}
