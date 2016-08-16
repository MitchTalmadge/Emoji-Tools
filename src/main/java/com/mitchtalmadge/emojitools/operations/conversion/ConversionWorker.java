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

package com.mitchtalmadge.emojitools.operations.conversion;

import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import com.mitchtalmadge.emojitools.operations.conversion.converter.Converter;

import java.io.File;

public class ConversionWorker extends OperationWorker {

    private final File conversionDirectory;
    private final ConversionInfo conversionInfo;

    public ConversionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File conversionDirectory, ConversionInfo conversionInfo) {
        super(operation, operationProgressDialog, false);
        this.conversionDirectory = conversionDirectory;
        this.conversionInfo = conversionInfo;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File[] files = conversionDirectory.listFiles();

        if (files == null)
        {
            showErrorDialog("Conversion Failed (Error Code 1)", "An internal error occurred. Please contact the developer for help.");
            return false;
        }

        Converter converter = new Converter();

        for (int i = 0; i < files.length; i++) {
            if (isCancelled()) {
                return false;
            }

            updateProgress(i, files.length);

            File file = files[i];
            if (file.getName().endsWith(".png")) {
                appendMessageToDialog("Converting " + file.getName());

                converter.convertFile(file, conversionInfo);
            }
        }

        return true;
    }
}
