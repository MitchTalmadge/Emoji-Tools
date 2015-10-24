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

package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.ConsoleManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.Tabs.*;
import me.MitchT.EmojiTools.Versioning;

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

public class EmojiToolsGUI extends JFrame implements MouseListener {

    private final ConsoleManager consoleManager;
    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JLabel headerLabel;
    private JLabel donateLabel;

    public EmojiToolsGUI(File fontFile) {

        setTitle(Versioning.PROGRAM_NAME);
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

        this.headerLabel.setText(Versioning.PROGRAM_NAME_WITH_VERSION);

        this.tabbedPane.addTab("Extractor", new ExtractionTab(this, fontFile));
        this.tabbedPane.addTab("Renamer", new RenamingTab(this));
        this.tabbedPane.addTab("Converter", new ConversionTab(this));
        this.tabbedPane.addTab("Packager", new PackagingTab(this));
        this.tabbedPane.setSelectedIndex(0);

        this.donateLabel.addMouseListener(this);
        this.donateLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
                Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=C4BS4EUTLUA9U&lc=US&item_name=Mitch%20Talmadge%20Code%20Donations&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
