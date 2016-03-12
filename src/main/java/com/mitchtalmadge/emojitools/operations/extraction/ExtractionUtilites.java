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

package com.mitchtalmadge.emojitools.operations.extraction;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class ExtractionUtilites {

    public static String getByteString(RandomAccessFile inputStream, int numBytesToRead) {
        byte[] bytes = new byte[numBytesToRead];
        try {
            inputStream.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bytes);
    }

    public static boolean compareBytes(RandomAccessFile inputStream, byte... compare) {
        byte[] bytes = new byte[compare.length];
        try {
            inputStream.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.equals(bytes, compare);
    }

}
