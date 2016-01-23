/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
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

package net.liveforcode.emojitools.operations.packaging;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.FontType;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;
import net.liveforcode.emojitools.operations.conversion.ConversionInfo;
import net.liveforcode.emojitools.operations.packaging.packagingthreads.ApplePackagingWorker;
import net.liveforcode.emojitools.operations.packaging.packagingthreads.GooglePackagingWorker;
import net.liveforcode.emojitools.operations.renaming.RenamingInfo;
import net.liveforcode.emojitools.operations.resizing.ResizingInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PackagingOperation extends Operation {

    public static final File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");

    private final File packagingDirectory;

    public PackagingOperation(File packagingDirectory) {
        this.packagingDirectory = packagingDirectory;
    }

    @Override
    protected OperationWorker getWorker() {
        //Check for an info file.
        File infoFile = new File(packagingDirectory, FontType.FONT_PROPERTIES_FILE_NAME);
        if (!infoFile.exists()) {
            EmojiTools.showErrorDialog("Cannot Package Selected Directory", "The selected directory does not appear to contain emojis extracted by EmojiTools. Did you modify the " + FontType.FONT_PROPERTIES_FILE_NAME + " file?");
            return null;
        }

        //Read info file
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(infoFile);
            properties.load(inputStream);
            String fontTypeProperty;
            if ((fontTypeProperty = properties.getProperty(FontType.FONT_PROPERTY_NAME)) != null) {
                FontType fontType = FontType.valueOf(fontTypeProperty);
                if (fontType != null) {
                    switch (fontType) {
                        case GOOGLE:
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
                        case APPLE:
                            if (EmojiTools.performRenamingOperation(packagingDirectory, new RenamingInfo(RenamingInfo.PREFIX_SET_U, RenamingInfo.CASE_UPPER, true)))
                                if (EmojiTools.performConversionOperation(packagingDirectory, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA))) {
                                    int[] sizes = new int[]{20, 32, 40, 48, 64, 96, 160};
                                    for (int size : sizes) {
                                        File setDir = new File(outputDirectory, "set_" + size);
                                        if (!EmojiTools.performResizingOperation(packagingDirectory, setDir, new ResizingInfo(size)))
                                            return null;
                                        else if (!EmojiTools.performConversionOperation(setDir, new ConversionInfo(ConversionInfo.DIRECTION_RGBA_CGBI)))
                                            return null;
                                    }
                                    return new ApplePackagingWorker(this, new OperationProgressDialog("Packaging to AppleColorEmoji@2x.ttf..."), packagingDirectory);
                                }
                            return null;
                        default:
                            EmojiTools.showErrorDialog("Cannot Package Selected Directory", "The selected directory cannot be packaged. The font type might not be supported. Contact a developer for help.");
                            return null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
