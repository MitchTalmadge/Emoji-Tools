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

package com.mitchtalmadge.emojitools.operations.extraction.extractors;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.FontType;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public abstract class ExtractionWorker extends OperationWorker {
    final static String[] standardOrderNames = {
            ".notdef", //0
            ".null",
            "nonmarkingreturn",
            "space",
            "exclam",
            "quotedbl",
            "numbersign",
            "dollar",
            "percent",
            "ampersand",
            "quotesingle",
            "parenleft",
            "parenright",
            "asterisk",
            "plus",
            "comma",
            "hyphen",
            "period",
            "slash",
            "zero",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "colon",
            "semicolon",
            "less",
            "equal",
            "greater",
            "question",
            "at",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z",
            "bracketleft",
            "backslash",
            "bracketright",
            "asciicircum",
            "underscore",
            "grave",
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "v",
            "w",
            "x",
            "y",
            "z",
            "braceleft",
            "bar",
            "braceright",
            "asciitilde",
            "Adieresis",
            "Aring",
            "Ccedilla",
            "Eacute",
            "Ntilde",
            "Odieresis",
            "Udieresis",
            "aacute",
            "agrave",
            "acircumflex",
            "adieresis",
            "atilde",
            "aring",
            "ccedilla",
            "eacute",
            "egrave",
            "ecircumflex",
            "edieresis",
            "iacute",
            "igrave",
            "icircumflex",
            "idieresis",
            "ntilde",
            "oacute",
            "ograve",
            "ocircumflex",
            "odieresis",
            "otilde",
            "uacute",
            "ugrave",
            "ucircumflex",
            "udieresis",
            "dagger",
            "degree",
            "cent",
            "sterling",
            "section",
            "bullet",
            "paragraph",
            "germandbls",
            "registered",
            "copyright",
            "trademark",
            "acute",
            "dieresis",
            "notequal",
            "AE",
            "Oslash",
            "infinity",
            "plusminus",
            "lessequal",
            "greaterequal",
            "yen",
            "mu",
            "partialdiff",
            "summation",
            "product",
            "pi",
            "integral",
            "ordfeminine",
            "ordmasculine",
            "Omega",
            "ae",
            "oslash",
            "questiondown",
            "exclamdown",
            "logicalnot",
            "radical",
            "florin",
            "approxequal",
            "Delta",
            "guillemotleft",
            "guillemotright",
            "ellipsis",
            "nonbreakingspace",
            "Agrave",
            "Atilde",
            "Otilde",
            "OE",
            "oe",
            "endash",
            "emdash",
            "quotedblleft",
            "quotedblright",
            "quoteleft",
            "quoteright",
            "divide",
            "lozenge",
            "ydieresis",
            "Ydieresis",
            "fraction",
            "currency",
            "guilsinglleft",
            "guilsinglright",
            "fi",
            "fl",
            "daggerdbl",
            "periodcentered",
            "quotesinglbase",
            "quotedblbase",
            "perthousand",
            "Acircumflex",
            "Ecircumflex",
            "Aacute",
            "Edieresis",
            "Egrave",
            "Iacute",
            "Icircumflex",
            "Idieresis",
            "Igrave",
            "Oacute",
            "Ocircumflex",
            "apple",
            "Ograve",
            "Uacute",
            "Ucircumflex",
            "Ugrave",
            "dotlessi",
            "circumflex",
            "tilde",
            "macron",
            "breve",
            "dotaccent",
            "ring",
            "cedilla",
            "hungarumlaut",
            "ogonek",
            "caron",
            "Lslash",
            "lslash",
            "Scaron",
            "scaron",
            "Zcaron",
            "zcaron",
            "brokenbar",
            "Eth",
            "eth",
            "Yacute",
            "yacute",
            "Thorn",
            "thorn",
            "minus",
            "multiply",
            "onesuperior",
            "twosuperior",
            "threesuperior",
            "onehalf",
            "onequarter",
            "threequarters",
            "franc",
            "Gbreve",
            "gbreve",
            "Idotaccent",
            "Scedilla",
            "scedilla",
            "Cacute",
            "cacute",
            "Ccaron",
            "ccaron",
            "dcroat" //257
    };
    final File fontFile;
    final File extractionDirectory;
    final List<String> tableNames;
    final List<Integer> tableOffsets;
    final List<Integer> tableLengths;

    public ExtractionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File fontFile, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths, boolean requireJython) {
        super(operation, operationProgressDialog, requireJython);
        this.fontFile = fontFile;
        this.extractionDirectory = extractionDirectory;
        this.tableNames = tableNames;
        this.tableOffsets = tableOffsets;
        this.tableLengths = tableLengths;
    }

    protected void writeFontTypeFile(FontType fontType, short[] resolutions) {
        File propertiesFile = new File(extractionDirectory, FontType.FONT_PROPERTIES_FILE_NAME);
        Properties properties = new Properties();
        properties.setProperty(FontType.FONT_PROPERTY_NAME, fontType.name());
        StringBuilder stringBuilder = new StringBuilder();
        if (resolutions != null) {
            for (short resolution : resolutions) {
                stringBuilder.append(resolution).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1); //Remove last comma
            properties.setProperty(FontType.FONT_RESOLUTIONS_PROPERTY_NAME, new String(stringBuilder));
        }

        try {
            properties.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException e) {
            EmojiTools.getLogManager().logSevere("Could not store properties for font file!");
            e.printStackTrace();
        }
    }

    @Override
    protected abstract Boolean doWork() throws Exception;
}
