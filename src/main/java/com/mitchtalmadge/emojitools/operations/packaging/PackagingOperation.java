/*
 * Copyright (C) 2015 - 2016 Mitch Talmadge (https://mitchtalmadge.com/)
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
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
 */

package com.mitchtalmadge.emojitools.operations.packaging;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import com.mitchtalmadge.emojitools.operations.packaging.packagingthreads.ApplePackagingWorker;
import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import com.mitchtalmadge.emojitools.operations.conversion.ConversionInfo;
import com.mitchtalmadge.emojitools.operations.packaging.packagingthreads.ApplePackagingWorker;
import com.mitchtalmadge.emojitools.operations.packaging.packagingthreads.GooglePackagingWorker;
import com.mitchtalmadge.emojitools.operations.renaming.RenamingInfo;
import com.mitchtalmadge.emojitools.operations.resizing.ResizingInfo;

import java.io.File;

public class PackagingOperation extends Operation {

    public static final File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");

    private final File packagingDirectory;
    private PackagingInfo packagingInfo;

    public PackagingOperation(File packagingDirectory, PackagingInfo packagingInfo) {
        this.packagingDirectory = packagingDirectory;
        this.packagingInfo = packagingInfo;
    }

    @Override
    protected OperationWorker getWorker() {
        if (!outputDirectory.exists())
            if (!outputDirectory.mkdir())
                return null;
        if (EmojiTools.performDeletionOperation(outputDirectory)) {

            switch (packagingInfo.getDeviceToPackageFor()) {
                case PackagingInfo.DEVICE_ANDROID:
                    //Check for .ttx file
                    File ttxFile = null;
                    File[] files = packagingDirectory.listFiles();
                    if (files == null)
                        return null;

                    for (File file : files) {
                        if (file.getName().equals("font.ttx")) {
                            ttxFile = file;
                            break;
                        }
                    }

                    if (ttxFile == null) {
                        return null;
                    }

                    if (EmojiTools.performRenamingOperation(packagingDirectory, new RenamingInfo(RenamingInfo.PREFIX_SET_UNI, RenamingInfo.CASE_LOWER, false)))
                        if (EmojiTools.performConversionOperation(packagingDirectory, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA)))
                            return new GooglePackagingWorker(this, new OperationProgressDialog("Packaging to NotoColorEmoji.ttf..."), packagingDirectory);
                    return null;
                case PackagingInfo.DEVICE_IOS:
                    if (EmojiTools.performRenamingOperation(packagingDirectory, new RenamingInfo(RenamingInfo.PREFIX_SET_U, RenamingInfo.CASE_UPPER, true)))
                        if (EmojiTools.performConversionOperation(packagingDirectory, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA))) {
                            short[] resolutions = packagingInfo.getResolutions();
                            if (resolutions == null)
                                resolutions = new short[]{20, 32, 40, 48, 64, 96, 160};
                            for (short resolution : resolutions) {
                                File setDir = new File(outputDirectory, "set_" + resolution);
                                if (!EmojiTools.performResizingOperation(packagingDirectory, setDir, new ResizingInfo(resolution)))
                                    return null;
                                else if (!EmojiTools.performConversionOperation(setDir, new ConversionInfo(ConversionInfo.DIRECTION_RGBA_CGBI)))
                                    return null;
                            }
                            return new ApplePackagingWorker(this, new OperationProgressDialog("Packaging to AppleColorEmoji@2x.ttf..."), packagingDirectory, resolutions);
                        }
                    return null;
                case PackagingInfo.DEVICE_OSX:
                    if (EmojiTools.performRenamingOperation(packagingDirectory, new RenamingInfo(RenamingInfo.PREFIX_SET_U, RenamingInfo.CASE_UPPER, true)))
                        if (EmojiTools.performConversionOperation(packagingDirectory, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA))) {
                            short[] resolutions = packagingInfo.getResolutions();
                            if (resolutions == null)
                                resolutions = new short[]{20, 32, 40, 48, 64, 96, 160};
                            for (short resolution : resolutions) {
                                File setDir = new File(outputDirectory, "set_" + resolution);
                                if (!EmojiTools.performResizingOperation(packagingDirectory, setDir, new ResizingInfo(resolution)))
                                    return null;
                            }
                            return new ApplePackagingWorker(this, new OperationProgressDialog("Packaging to AppleColorEmoji@2x.ttf..."), packagingDirectory, resolutions);
                        }
                    return null;
                default:
                    EmojiTools.showErrorDialog("Cannot Package Selected Directory", "The selected directory cannot be packaged. The font type might not be supported. Contact a developer for help.");
                    return null;
            }
        }
        return null;
    }
}
