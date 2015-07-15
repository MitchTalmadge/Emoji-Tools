package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import java.awt.*;

public class PackagingDialog extends ProgressDialog {

    public PackagingDialog(OperationTab gui, Image logo) {
        super(gui, "Packaging Emojis to NotoColorEmoji.ttf", logo);
    }

}
