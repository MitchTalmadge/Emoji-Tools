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

package net.liveforcode.EmojiTools.Packaging;

import net.liveforcode.EmojiTools.EmojiTools;
import net.liveforcode.EmojiTools.GUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools.GUI.PackagingDialog;
import net.liveforcode.EmojiTools.GUI.Tabs.PackagingTab;
import net.liveforcode.EmojiTools.JythonHandler;
import net.liveforcode.EmojiTools.OperationManager;
import net.liveforcode.EmojiTools.Packaging.PackagingThreads.AndroidPackagingThread;
import net.liveforcode.EmojiTools.Packaging.PackagingThreads.PackagingThread;

import java.io.File;

public class PackagingManager extends OperationManager implements EmojiTools.JythonListener {

    private final EmojiToolsGUI gui;
    private final File pngDirectory;
    private final PackagingDialog packagingDialog;
    private final int outputType;
    private PackagingThread packagingThread;

    public PackagingManager(EmojiToolsGUI gui, File pngDirectory, PackagingDialog packagingDialog, int outputType) {
        this.gui = gui;
        this.pngDirectory = pngDirectory;
        this.packagingDialog = packagingDialog;
        this.outputType = outputType;
    }

    @Override
    public void start() {
        if (this.outputType == PackagingTab.ANDROID) { //TODO: Implement iOS and OSX Emoji Fonts
            packagingDialog.setIndeterminate(true);
            packagingDialog.appendToStatus("Compiling Scripts... (This can take a minute. Please Wait...)");

            EmojiTools.addJythonListener(this);
        }
    }

    @Override
    public void onJythonReady(JythonHandler jythonHandler) {
        this.packagingThread = new AndroidPackagingThread(gui, pngDirectory, this, packagingDialog, jythonHandler);
        this.gui.getConsoleManager().addConsoleListener(packagingThread);
        this.packagingThread.start();
    }

    @Override
    public void stop() {
        if (packagingThread != null && packagingThread.isAlive()) {
            packagingThread.endPackaging();
        }
    }

}
