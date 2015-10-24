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

package me.MitchT.EmojiTools.Packaging;

import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.PackagingDialog;
import me.MitchT.EmojiTools.OperationManager;

import java.io.File;

public class PackagingManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final PackagingThread packagingThread;
    private final PackagingDialog packagingDialog;

    public PackagingManager(EmojiToolsGUI gui, File pngDirectory, PackagingDialog packagingDialog, int outputType) {
        this.gui = gui;
        this.packagingDialog = packagingDialog;

        this.packagingThread = new PackagingThread(gui, pngDirectory, this, packagingDialog, outputType);
    }

    @Override
    public void start() {
        this.packagingThread.start();
    }

    @Override
    public void stop() {
        if (packagingThread != null && packagingThread.isAlive()) {
            packagingThread.endPackaging();
        }
    }

}
