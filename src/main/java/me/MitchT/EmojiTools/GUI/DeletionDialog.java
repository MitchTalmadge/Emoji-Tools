package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import java.awt.*;

public class DeletionDialog extends ProgressDialog {

    public DeletionDialog(ExtractionTab gui, Image logo) {
        super(gui, "Deleting Extraction Directory Contents", logo);
    }

}
