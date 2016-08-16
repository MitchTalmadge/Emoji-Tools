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

package com.mitchtalmadge.emojitools.operations.renaming;

public class RenamingInfo {

    public static final int PREFIX_DONT_CHANGE = 0;
    public static final int PREFIX_REMOVE_ALL = 1;
    public static final int PREFIX_SET_UNI = 2;
    public static final int PREFIX_SET_U = 3;

    public static final int CASE_DONT_CHANGE = 0;
    public static final int CASE_UPPER = 1;
    public static final int CASE_LOWER = 2;

    private final int prefixOption;
    private final int caseOption;
    private final boolean oppositePrefixCase;

    public RenamingInfo(int prefixOption, int caseOption, boolean oppositePrefixCase) {
        if (prefixOption < 0 || prefixOption > 3) {
            prefixOption = 0;
        }
        if (caseOption < 0 || caseOption > 2) {
            caseOption = 0;
        }

        this.prefixOption = prefixOption;
        this.caseOption = caseOption;
        this.oppositePrefixCase = oppositePrefixCase;
    }

    public int getPrefixOption() {
        return prefixOption;
    }

    public int getCaseOption() {
        return caseOption;
    }

    public boolean isOppositePrefixCase() {
        return oppositePrefixCase;
    }
}
