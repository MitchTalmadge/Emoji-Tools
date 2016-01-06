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

package net.liveforcode.emojitools.gui;

import javafx.scene.control.TextField;

/**
 * This custom Java FX TextField allows for the developer to specify a regex string which all inputted text
 * must match in order to pass. Text which does not match the specified regex string will not be processed.
 * The developer can also specify a maximum length for the input using setMaxLength(int)
 * <p>
 * Use setRegexLimiter(String) to specify the regex limiter after instantiation.
 * By default it is ".*" (Any character).
 */
public class LimitingTextField extends TextField {

    private String regexLimiter = ".*";
    private int maxLength;

    public LimitingTextField() {
        super();
    }

    public LimitingTextField(String text) {
        super(text);
    }

    /**
     * Sets the regex limiter that all inputted text must match.
     *
     * @param regex The regex string to match.
     */
    public void setRegexLimiter(String regex) {
        this.regexLimiter = regex;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        int amountToAdd = text.length() - (end - start);
        int newLength = getText().length() + amountToAdd;
        boolean isAtOrBelowMaxLength =  newLength <= maxLength;

        if (text.matches(regexLimiter))
            super.replaceText(start, end, isAtOrBelowMaxLength ? text : text.substring(0, text.length() - (newLength - maxLength)));
    }

}
