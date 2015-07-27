package com.AptiTekk.AptiAPI;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AptiCrypto {
    //iv length should be 16 bytes
    private String iv = "fedcba9876543210";
    private String key = null;
    private Cipher cipher = null;
    private SecretKeySpec keySpec = null;
    private IvParameterSpec ivSpec = null;

    public AptiCrypto(String key) throws Exception {
        this.key = key;

        // Make sure the key length should be 16
        int len = this.key.length();
        if (len < 16) {
            int addSpaces = 16 - len;
            for (int i = 0; i < addSpaces; i++) {
                this.key = this.key + " ";
            }
        } else {
            this.key = this.key.substring(0, 16);
        }
        this.keySpec = new SecretKeySpec(this.key.getBytes(), "AES");
        this.ivSpec = new IvParameterSpec(iv.getBytes());
        this.cipher = Cipher.getInstance("AES/CBC/NoPadding");
    }

    public String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        } else {
            int len = data.length;
            String str = "";
            for (byte aData : data) {
                if ((aData & 0xFF) < 16)
                    str = str + "0" + Integer.toHexString(aData & 0xFF);
                else
                    str = str + Integer.toHexString(aData & 0xFF);
            }
            return str;
        }
    }

    public String encrypt(String plainData) throws Exception {

        // Make sure the plainData length should be multiple with 16
        int len = plainData.length();
        int q = len / 16;
        int addSpaces = ((q + 1) * 16) - len;
        for (int i = 0; i < addSpaces; i++) {
            plainData = plainData + " ";
        }

        this.cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, this.ivSpec);
        byte[] encrypted = cipher.doFinal(plainData.getBytes());

        return bytesToHex(encrypted);
    }

}
