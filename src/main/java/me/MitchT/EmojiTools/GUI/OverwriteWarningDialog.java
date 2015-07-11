package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class OverwriteWarningDialog extends JDialog implements ActionListener {
    private final ExtractionTab gui;
    private JPanel contentPane;
    private JButton backButton;
    private JButton continuebutton;
    private JTextField extractionDirectoryField;

    public OverwriteWarningDialog(ExtractionTab gui, Image logo, File extractionDirectory) {

        this.gui = gui;

        this.setIconImage(logo);
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(backButton);

        this.extractionDirectoryField.setText(extractionDirectory.getName());

        backButton.addActionListener(this);
        continuebutton.addActionListener(this);

        // call onBack() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onBack();
            }
        });

        // call onBack() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        this.setLocationRelativeTo(gui);
    }

    private void onBack() {
        gui.stopOperations();
        this.dispose();
    }

    private void onContinue() {
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.backButton))
            onBack();
        else if (e.getSource().equals(continuebutton))
            onContinue();
    }
}
