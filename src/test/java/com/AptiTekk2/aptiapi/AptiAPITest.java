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

package com.AptiTekk2.aptiapi;

import com.aptitekk.aptiapi.AptiAPI;
import com.aptitekk.aptiapi.AptiAPIListener;
import com.aptitekk.aptiapi.ErrorReport;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.Versioning;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class AptiAPITest {

    private AptiAPI aptiAPI;
    private TestAPIListener listener;

    @Before
    public void setUp() throws Exception {
        aptiAPI = new AptiAPI(new Versioning(), EmojiTools.getLogoImage());
        listener = new TestAPIListener();
    }

    @Test
    public void testSendErrorReport() throws Exception {
        ErrorReport errorReport = new ErrorReport(new Thread("Test Thread"), new Exception("Automated Test Exception"));
        boolean worked = aptiAPI.sendErrorReport(errorReport);
        assertEquals("Send Error Report Worked", worked, true);
    }

    @Test
    public void testAddAndRemoveAPIListener() throws Exception {
        aptiAPI.APIListeners.clear(); // Clear any previous listeners
        aptiAPI.addAPIListener(listener);

        ArrayList<AptiAPIListener> apiListeners = aptiAPI.APIListeners;
        assertTrue("APIListeners contains listener", apiListeners.contains(listener));

        aptiAPI.removeAPIListener(listener);
        assertFalse("listener removed from APIListeners", apiListeners.contains(listener));
    }

    private class TestAPIListener implements AptiAPIListener {

        @Override
        public void displayInfo(String message) {

        }

        @Override
        public void displayError(String message) {

        }

        @Override
        public void shutdown() {

        }
    }
}