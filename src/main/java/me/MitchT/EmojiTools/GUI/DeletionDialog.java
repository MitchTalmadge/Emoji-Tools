package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import java.awt.*;

public class DeletionDialog extends ProgressDialog {

    public DeletionDialog(OperationTab gui, Image logo) {
        super(gui, "Deleting Extraction Directory Contents", logo);
    }

}
