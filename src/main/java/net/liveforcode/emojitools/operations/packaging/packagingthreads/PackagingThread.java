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

package net.liveforcode.emojitools.operations.packaging.packagingthreads;

import net.liveforcode.emojitools.ConsoleManager;
import net.liveforcode.emojitools.oldgui.EmojiToolsGUI;
import net.liveforcode.emojitools.oldgui.PackagingDialog;
import net.liveforcode.emojitools.JythonHandler;
import net.liveforcode.emojitools.operations.packaging.PackagingOperation;

import java.io.File;

public class PackagingThread extends Thread implements ConsoleManager.ConsoleListener {

    final EmojiToolsGUI gui;
    final File pngDirectory;
    final PackagingOperation packagingOperationManager;
    final PackagingDialog packagingDialog;
    final JythonHandler jythonHandler;

    boolean running = true;

    public PackagingThread(String threadName, EmojiToolsGUI gui, File pngDirectory, PackagingOperation packagingOperationManager, PackagingDialog packagingDialog, JythonHandler jythonHandler) {
        super(threadName);
        this.gui = gui;
        this.pngDirectory = pngDirectory;
        this.packagingOperationManager = packagingOperationManager;
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
