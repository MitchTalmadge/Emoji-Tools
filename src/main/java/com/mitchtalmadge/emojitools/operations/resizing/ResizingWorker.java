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

package com.mitchtalmadge.emojitools.operations.resizing;

import com.mitchtalmadge.emojitools.operations.OperationWorker;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import org.imgscalr.Scalr;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ResizingWorker extends OperationWorker {

    private final File resizingSourceDirectory;
    private final File resizingDestDirectory;
    private final ResizingInfo resizingInfo;

    public ResizingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File resizingSourceDirectory, File resizingDestDirectory, ResizingInfo resizingInfo) {
        super(operation, operationProgressDialog, false);
        this.resizingSourceDirectory = resizingSourceDirectory;
        this.resizingDestDirectory = resizingDestDirectory;
        this.resizingInfo = resizingInfo;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File[] files = resizingSourceDirectory.listFiles(file -> file.getName().toLowerCase().endsWith(".png"));

        if (files == null) {
            showErrorDialog("Resizing Failed (Error Code 1)", "An internal error occurred. Please contact the developer for help.");
            return false;
        }

        if (!resizingDestDirectory.exists() && !resizingDestDirectory.mkdir()) {
            showErrorDialog("Unable to Create Directory", "Emoji Tools was unable to create a required directory. Does it have permission?");
            return false;
        } else {
            File[] oldFiles = resizingDestDirectory.listFiles();
            if (oldFiles == null) {
                showErrorDialog("Resizing Failed (Error Code 2)", "An internal error occurred. Please contact the developer for help.");
                return false;
            }
            for (File file : oldFiles) {
                if (!file.delete()) {
                    showErrorDialog("Could Not Delete File", "Emoji Tools was unable to delete a file that must be deleted. Does it have permission?");
                }
            }
        }

        for (int i = 0; i < files.length; i++) {
            if (isCancelled())
                return false;

            try {
                BufferedImage image = ImageIO.read(files[i]);
                image = Scalr.resize(image, resizingInfo.getNewMaxSize());
                File scaledImageFile = new File(resizingDestDirectory, files[i].getName());
                ImageIO.write(image, "png", scaledImageFile);
            } catch (IIOException ignored) {
            }

            appendMessageToDialog("Resizing Emoji "+i+" of "+files.length+"...");
            updateProgress(i, files.length);
        }

        updateProgress(files.length, files.length);

        return true;
    }

}
