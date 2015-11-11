/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package net.liveforcode.EmojiTools.GUI;

import com.AptiTekk.AptiAPI.AptiAPI;
import com.AptiTekk.AptiAPI.AptiAPIListener;
import net.liveforcode.EmojiTools.ConsoleManager;
import net.liveforcode.EmojiTools.EmojiTools;
import net.liveforcode.EmojiTools.GUI.Tabs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class EmojiToolsGUI extends JFrame implements MouseListener, AptiAPIListener {

    private final ConsoleManager consoleManager;
    private final AptiAPI aptiAPI;
    private final ExtractionTab extractionTab;
    private final RenamingTab renamingTab;
    private final ConversionTab conversionTab;
    private final PackagingTab packagingTab;
    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JLabel headerLabel;
    private JLabel donateLabel;
    private JLabel copyrightLabel;

    public EmojiToolsGUI(AptiAPI aptiAPI, File fontFile) {
        this.aptiAPI = aptiAPI;

        setTitle(aptiAPI.getVersioningDetails().getProgramName());
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

        this.headerLabel.setText(aptiAPI.getVersioningDetails().getProgramNameWithVersion());

        this.extractionTab = new ExtractionTab(this, fontFile);
        this.renamingTab = new RenamingTab(this);
        this.conversionTab = new ConversionTab(this);
        this.packagingTab = new PackagingTab(this);

        this.tabbedPane.addTab("Extractor", extractionTab);
        this.tabbedPane.addTab("Renamer", renamingTab);
        this.tabbedPane.addTab("Converter", conversionTab);
        this.tabbedPane.addTab("Packager", packagingTab);
        this.tabbedPane.setSelectedIndex(0);

        this.donateLabel.addMouseListener(this);
        this.donateLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        this.copyrightLabel.addMouseListener(this);
        this.copyrightLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        this.consoleManager = new ConsoleManager();

        this.aptiAPI.addAPIListener(this);

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

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource().equals(this.donateLabel))
            try {
                Desktop.getDesktop().browse(new URI("https://donate.liveforcode.net"));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        else if (e.getSource().equals(this.copyrightLabel)) {
            try {
                Desktop.getDesktop().browse(new URI("https://liveforcode.net"));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void displayInfo(final String message) {
        JOptionPane.showMessageDialog(null, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void displayError(final String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void shutdown() {
        this.extractionTab.stopOperations();
        this.renamingTab.stopOperations();
        this.conversionTab.stopOperations();
        this.packagingTab.stopOperations();
    }
}
