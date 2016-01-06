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

package net.liveforcode.emojitools;

import com.aptitekk.aptiapi.AptiAPIVersioningDetails;

public class Versioning implements AptiAPIVersioningDetails {

    private static final double VERSION = 1.9;
    private static final String VERSION_STRING = VERSION + "";
    private static final String PROGRAM_NAME = "Emoji Tools";
    private static final String PROGRAM_NAME_WITH_VERSION = PROGRAM_NAME + " V" + VERSION_STRING;
    private static final int APTIAPI_PROJECT_ID = 1;
    private static final int APTIAPI_VERSION_ID = 1;

    @Override
    public double getVersion() {
        return VERSION;
    }

    @Override
    public String getVersionString() {
        return VERSION_STRING;
    }

    @Override
    public String getProgramName() {
        return PROGRAM_NAME;
    }

    @Override
    public String getProgramNameWithVersion() {
        return PROGRAM_NAME_WITH_VERSION;
    }

    @Override
    public int getAptiAPIProjectID() {
        return APTIAPI_PROJECT_ID;
    }

    @Override
    public int getAptiAPIVersionID() {
        return APTIAPI_VERSION_ID;
    }
}
