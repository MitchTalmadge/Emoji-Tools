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

package com.mitchtalmadge.emojitools.operations.packaging;

import java.util.HashMap;
import java.util.List;

public class LigatureSet {

    private final String mainGlyphName;
    private final HashMap<List<String>, String> ligatureComponentsGlyphMap = new HashMap<>();

    public LigatureSet(String mainGlyphName)
    {
        this.mainGlyphName = mainGlyphName;
    }

    public String getMainGlyphName()
    {
        return mainGlyphName;
    }

    public void assignComponentsToGlyph(List<String> components, String glyph)
    {
        ligatureComponentsGlyphMap.put(components, glyph);
    }

    public String getGlyphNameFromComponents(List<String> components)
    {
        return ligatureComponentsGlyphMap.get(components);
    }

}
