package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import java.awt.*;

public class RenamingDialog extends ProgressDialog {

    public RenamingDialog(OperationTab gui, Image logo) {
        super(gui, "Renaming Emojis", logo);
    }

}
