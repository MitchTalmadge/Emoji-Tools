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

package net.liveforcode.EmojiTools.Packaging.PackagingThreads;

import net.liveforcode.EmojiTools.ConsoleManager;
import net.liveforcode.EmojiTools.OldGUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools.OldGUI.PackagingDialog;
import net.liveforcode.EmojiTools.JythonHandler;
import net.liveforcode.EmojiTools.Packaging.PackagingManager;

import java.io.File;

public class PackagingThread extends Thread implements ConsoleManager.ConsoleListener {

    final EmojiToolsGUI gui;
    final File pngDirectory;
    final PackagingManager packagingManager;
    final PackagingDialog packagingDialog;
    final JythonHandler jythonHandler;

    boolean running = true;

    public PackagingThread(String threadName, EmojiToolsGUI gui, File pngDirectory, PackagingManager packagingManager, PackagingDialog packagingDialog, JythonHandler jythonHandler) {
        super(threadName);
        this.gui = gui;
        this.pngDirectory = pngDirectory;
        this.packagingManager = packagingManager;
        this.packagingDialog = packagingDialog;
        this.jythonHandler = jythonHandler;
    }

    public void endPackaging() {
        running = false;
    }

    @Override
    public void write(String message) {
        this.packagingDialog.writeToStatus(message);
    }
}
