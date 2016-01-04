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

package net.liveforcode.emojitools.operations.conversion;

import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;

import java.io.File;

public class ConversionOperation extends Operation {

    private final File conversionDirectory;
    private final ConversionInfo conversionInfo;

    public ConversionOperation(File conversionDirectory, ConversionInfo conversionInfo) {
        this.conversionDirectory = conversionDirectory;
        this.conversionInfo = conversionInfo;
    }

    @Override
    protected OperationWorker getWorker() {
        return new ConversionWorker(this, new OperationProgressDialog("Converting Emojis..."), conversionDirectory, conversionInfo);
    }
}
