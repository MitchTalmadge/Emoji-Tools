package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.OperationManager;

import javax.swing.*;

public class OperationTab extends JPanel {

    OperationManager currentOperationManager;
    boolean cancelled;

    public void stopOperations() {
        if (this.currentOperationManager != null)
            this.currentOperationManager.stop();
        this.cancelled = true;
    }

}
