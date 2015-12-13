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
import net.liveforcode.EmojiTools.Extraction.ExtractionManager;
import net.liveforcode.EmojiTools.GUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools.GUI.PackagingDialog;
import net.liveforcode.EmojiTools.JythonHandler;
import net.liveforcode.EmojiTools.OperationManager;
import net.liveforcode.EmojiTools.Packaging.PackagingThreads.AndroidPackagingThread;
import net.liveforcode.EmojiTools.Packaging.PackagingThreads.PackagingThread;

import java.io.File;

public class PackagingManager extends OperationManager implements EmojiTools.JythonListener {

    private final EmojiToolsGUI gui;
    private final File pngDirectory;
    private final PackagingDialog packagingDialog;
    private PackagingThread packagingThread;

    private ExtractionManager.TTXType ttxType;

    public PackagingManager(EmojiToolsGUI gui, File pngDirectory, PackagingDialog packagingDialog, ExtractionManager.TTXType ttxType) {
        this.gui = gui;
        this.pngDirectory = pngDirectory;
        this.packagingDialog = packagingDialog;
        this.ttxType = ttxType;
    }

    @Override
    public void start() {
        packagingDialog.setIndeterminate(true);
        packagingDialog.appendToStatus("Compiling Scripts... (This can take a minute. Please Wait...)");

        switch (ttxType) {
            case ANDROID:
                EmojiTools.addJythonListener(this);
                break;
            case IOS:
            case OSX:
                showMessageDialog("iOS and OSX Emoji Fonts cannot be created yet. This feature is in development.");
                break;
            default:
                showMessageDialog("The selected Emoji directory is invalid or cannot be packaged.");
                break;
        }
    }

    @Override
    public void onJythonReady(JythonHandler jythonHandler) {
        switch (ttxType) {
            case ANDROID:
                this.packagingThread = new AndroidPackagingThread(gui, pngDirectory, this, packagingDialog, jythonHandler);
                this.gui.getConsoleManager().addConsoleListener(packagingThread);
                packagingThread.start();
                break;
            case IOS:
            case OSX:
            default:
                break;
        }

    }

    @Override
    public void stop() {
        if (packagingThread != null && packagingThread.isAlive()) {
            packagingThread.endPackaging();
        }
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }
}
