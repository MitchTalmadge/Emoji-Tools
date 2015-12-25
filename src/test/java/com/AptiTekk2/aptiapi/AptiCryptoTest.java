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

import com.aptitekk.aptiapi.AptiCrypto;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AptiCryptoTest {

    private AptiCrypto aptiCrypto;

    @Before
    public void setUp() throws Exception {
        aptiCrypto = new AptiCrypto("TestKey");
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String message = "TestMessage";

        String encryptedMessage = aptiCrypto.encrypt(message);
        assertNotNull("Encrypt message", encryptedMessage);

        String decryptedMessage = aptiCrypto.decrypt(encryptedMessage);
        assertNotNull("Decrypt message", decryptedMessage);

        assertEquals("Decrypted message equals original message", decryptedMessage, message);
    }
}