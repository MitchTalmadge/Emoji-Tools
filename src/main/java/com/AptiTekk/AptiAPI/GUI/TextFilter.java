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

package com.AptiTekk.AptiAPI.GUI;

import javax.swing.text.*;
import java.util.ArrayList;

public class TextFilter {

    public static final int ALPHA = 0;
    public static final int NUMERIC = 1;
    public static final int ALPHANUMERIC = 2;
    public static final int ALPHA_SPACE = 3;
    public static final int NUMERIC_SPACE = 4;
    public static final int ALPHANUMERIC_SPACE = 5;
    public static final int NO_HTML = 7;
    public static final int EMAIL = 8;
    public static final int FILENAME = 9;

    private static final ArrayList<TextFilterListener> listenerList = new ArrayList<>();

    public static void assignFilter(JTextComponent textComponent, int maxLength, int filterType, TextFilterListener listener) {
        if (listener != null && !listenerList.contains(listener))
            listenerList.add(listener);
        ((AbstractDocument) textComponent.getDocument()).setDocumentFilter(new TextComponentFilter(textComponent, maxLength, filterType, listener));
    }

    public interface TextFilterListener {
        void lengthChanged(int newLength, JTextComponent sourceComponent);
    }

    private static class TextComponentFilter extends DocumentFilter {

        private final JTextComponent textComponent;
        private final int maxLength;
        private final int filterType;
        private final TextFilterListener listener;

        public TextComponentFilter(JTextComponent textComponent, int maxLength, int filterType, TextFilterListener listener) {
            this.textComponent = textComponent;
            this.maxLength = maxLength;
            this.filterType = filterType;
            this.listener = listener;
        }

        @Override
        public void replace(FilterBypass fb, int i, int i1, String string, AttributeSet as) throws BadLocationException {
            for (int n = string.length(); n > 0; n--) {
                char c = string.charAt(n - 1);
                switch (filterType) {
                    case ALPHA:
                        if ((Character.isAlphabetic(c)) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case NUMERIC:
                        if ((Character.isDigit(c)) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case ALPHANUMERIC:
                        if ((Character.isAlphabetic(c) || Character.isDigit(c)) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case ALPHA_SPACE:
                        if ((Character.isAlphabetic(c) || c == ' ') && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case NUMERIC_SPACE:
                        if ((Character.isDigit(c) || c == ' ') && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case ALPHANUMERIC_SPACE:
                        if ((Character.isAlphabetic(c) || Character.isDigit(c) || c == ' ') && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case NO_HTML:
                        if ((!isOneOfChars(c, '<', '>', '&')) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case EMAIL:
                        if ((Character.isAlphabetic(c) || Character.isDigit(c) || isOneOfChars(c, '.', '!', '@', '_', '-', '+', '#', '$', '?', '=', '%', '\'', '*', '^', '`', '{', '|', '}', '~')) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    case FILENAME:
                        if ((Character.isAlphabetic(c) || Character.isDigit(c) || isOneOfChars(c, '_', '-')) && (maxLength < 0 || this.textComponent.getText().length() < maxLength))
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                    default:
                        if (maxLength < 0 || this.textComponent.getText().length() < maxLength)
                            super.replace(fb, i, i1, String.valueOf(c), as);
                        break;
                }
            }
            if (listener != null)
                listener.lengthChanged(textComponent.getText().length(), textComponent);
        }

        @Override
        public void remove(FilterBypass fb, int i, int i1) throws BadLocationException {
            super.remove(fb, i, i1);

            if (listener != null)
                listener.lengthChanged(textComponent.getText().length(), textComponent);
        }

        private boolean isOneOfChars(char c, char... chars) {
            for (char i : chars)
                if (c == i)
                    return true;
            return false;
        }

    }

}
