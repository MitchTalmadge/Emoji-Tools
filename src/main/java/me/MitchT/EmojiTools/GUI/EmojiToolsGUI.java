package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ConversionTab;
import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;
import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;
import me.MitchT.EmojiTools.GUI.Tabs.RenamingTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class EmojiToolsGUI extends JFrame {

    private final String version = "V1.5";
    private final Image logo;

    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JLabel headerLabel;

    public EmojiToolsGUI(File fontFile) {

        setTitle("Emoji Tools");
        setContentPane(contentPane);
        setResizable(false);

        this.logo = new ImageIcon(getClass().getResource("/Images/EmojiToolsLogo.png")).getImage();

        this.setIconImage(logo);

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

        this.headerLabel.setText("Emoji Tools " + version);

        this.tabbedPane.addTab("Extractor", new ExtractionTab(this, fontFile));
        this.tabbedPane.addTab("Renamer", new RenamingTab(this));
        this.tabbedPane.addTab("Converter", new ConversionTab(this));
        this.tabbedPane.setSelectedIndex(0);

        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public Image getLogo() {
        return this.logo;
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
