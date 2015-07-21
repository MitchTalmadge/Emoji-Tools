package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.ConsoleManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.Tabs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class EmojiToolsGUI extends JFrame {

    private final ConsoleManager consoleManager;
    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JLabel headerLabel;

    public EmojiToolsGUI(File fontFile) {

        setTitle("Emoji Tools");
        setContentPane(contentPane);
        setResizable(false);

        this.setIconImage(EmojiTools.getLogoImage());

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int tabCount = tabbedPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    Component component = tabbedPane.getComponentAt(i);
                    if (component instanceof OperationTab)
                        ((OperationTab) component).stopOperations();
                }
                System.exit(0);
            }
        });

        this.headerLabel.setText("Emoji Tools " + EmojiTools.version);

        this.tabbedPane.addTab("Extractor", new ExtractionTab(this, fontFile));
        this.tabbedPane.addTab("Renamer", new RenamingTab(this));
        this.tabbedPane.addTab("Converter", new ConversionTab(this));
        this.tabbedPane.addTab("Packager", new PackagingTab(this));
        this.tabbedPane.setSelectedIndex(0);

        this.consoleManager = new ConsoleManager();

        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public ConsoleManager getConsoleManager() {
        return this.consoleManager;
    }
}
