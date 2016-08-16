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

package com.mitchtalmadge.emojitools.operations.extraction;

import java.util.List;

public class Ligature {

    private String ligatureGlyphName;

    private String setGlyphName;
    private List<String> components;

    public Ligature(String ligatureGlyphName, String setGlyphName, List<String> components){
        this.ligatureGlyphName = ligatureGlyphName;
        this.setGlyphName = setGlyphName;
        this.components = components;
    }

    public String getLigatureGlyphName() {
        return ligatureGlyphName;
    }

    public String getSetGlyphName() {
        return setGlyphName;
    }

    public List<String> getComponents() {
        return components;
    }
}
