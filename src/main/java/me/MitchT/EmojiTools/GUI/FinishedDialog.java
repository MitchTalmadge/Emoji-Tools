package me.MitchT.EmojiTools.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class FinishedDialog extends JDialog implements ActionListener {
    private final File extractionDirectory;
    private JPanel contentPane;
    private JButton OKButton;
    private JButton openEmojiDirectoryButton;
    private JTextField extractionLocationField;

    public FinishedDialog(EmojiToolsGUI gui, Image logo, File extractionDirectory) {
        this.extractionDirectory = extractionDirectory;

        this.setIconImage(logo);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(OKButton);

        this.extractionLocationField.setText(extractionDirectory.getAbsolutePath());

        OKButton.addActionListener(this);

        // call onOK() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onOK() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        openEmojiDirectoryButton.addActionListener(this);

        pack();
        this.setLocationRelativeTo(gui);
    }

    private void onOK() {
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(OKButton)) {
            onOK();
        } else if (e.getSource().equals(openEmojiDirectoryButton)) {
            try {
                Desktop.getDesktop().open(extractionDirectory);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            this.dispose();
        }
    }
}
