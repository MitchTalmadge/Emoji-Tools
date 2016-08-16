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

package com.mitchtalmadge.emojitools;

public class Versioning {

    private static final double VERSION = 1.91;
    private static final String VERSION_STRING = VERSION + "";
    private static final String PROGRAM_NAME = "Emoji Tools";
    private static final String PROGRAM_NAME_WITH_VERSION = PROGRAM_NAME + " V" + VERSION_STRING;

    public double getVersion() {
        return VERSION;
    }

    public String getVersionString() {
        return VERSION_STRING;
    }

    public String getProgramName() {
        return PROGRAM_NAME;
    }

    public String getProgramNameWithVersion() {
        return PROGRAM_NAME_WITH_VERSION;
    }
}
