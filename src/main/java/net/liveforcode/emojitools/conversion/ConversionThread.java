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

package net.liveforcode.emojitools.conversion;

import net.liveforcode.emojitools.conversion.converter.Converter;
import net.liveforcode.emojitools.oldgui.ConversionDialog;

import java.io.File;

class ConversionThread extends Thread {

    private final File conversionFile;
    private final ConversionManager conversionManager;
    private final ConversionDialog conversionDialog;
    private final boolean CgBItoRGBA;
    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public ConversionThread(File conversionFile, ConversionManager conversionManager, ConversionDialog conversionDialog, boolean CgBItoRGBA) {
        super("ConversionThread");
        this.conversionFile = conversionFile;
        this.conversionManager = conversionManager;
        this.conversionDialog = conversionDialog;
        this.CgBItoRGBA = CgBItoRGBA;
    }

    @Override
    public void run() {

        File[] files;

        if (conversionFile.isDirectory()) {
            files = conversionFile.listFiles();
        } else {
            files = new File[]{conversionFile};
        }

        totalFileNum = files.length;

        Converter converter = new Converter();

        for (File file : files) {
            if (!running) {
                conversionDialog.dispose();
                return;
            }

            updateProgress();

            currentFileNum++;

            if (file.getName().endsWith(".png")) {
                System.out.println("Converting " + file.getName());
                conversionDialog.appendToStatus("Converting " + file.getName());

                converter.convertFile(file, CgBItoRGBA);
            }
        }

        updateProgress();

        conversionDialog.dispose();
    }

    private void updateProgress() {
        conversionDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endConversion() {
        running = false;
    }
}
