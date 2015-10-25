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

package net.liveforcode.EmojiTools.Renaming;

import net.liveforcode.EmojiTools.GUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools.GUI.RenamingDialog;
import net.liveforcode.EmojiTools.OperationManager;

import java.io.File;

public class RenamingManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final RenamingThread renamingThread;


    public RenamingManager(File renameFile, EmojiToolsGUI gui, RenamingDialog renamingDialog, boolean[] prefixButtons, boolean[] capitalizationButtons) {
        this.gui = gui;

        this.renamingThread = new RenamingThread(renameFile, this, renamingDialog, prefixButtons, capitalizationButtons);
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }

    @Override
    public void start() {
        renamingThread.start();
    }

    @Override
    public void stop() {
        if (renamingThread != null && renamingThread.isAlive()) {
            renamingThread.endRenaming();
        }
    }
}
