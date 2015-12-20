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

package com.AptiTekk.AptiAPI.GUI;

import com.AptiTekk.AptiAPI.AptiAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UpdateNoticeDialog extends JDialog {
    private AptiAPI aptiAPI;
    private final String versionName;
    private final String changeLog;
    private final String downloadURL;

    private JPanel contentPane;
    private JButton okButton;
    private JButton downloadButton;
    private JLabel headerLabel;
    private JScrollPane changeLogScrollPane;
    private JTextPane changeLogTextPane;

    public UpdateNoticeDialog(AptiAPI aptiAPI, String versionName, String changeLog, String downloadURL) {
        this.aptiAPI = aptiAPI;
        this.versionName = versionName;
        this.changeLog = changeLog;
        this.downloadURL = downloadURL;

        setTitle("Update Released!");
        setIconImage(aptiAPI.getIconImage());
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(okButton);

        headerLabel.setText("Emoji Tools V"+versionName+" has been Released!");
        changeLogTextPane.setText("<html><body style=\"font-family: Arial, Helvetica, sans-serif\">"+changeLog+"</body></html>");

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDownload();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void onOK() {
        dispose();
    }

    private void onDownload() {
        try {
            Desktop.getDesktop().browse(new URI(downloadURL));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        dispose();
    }
}
