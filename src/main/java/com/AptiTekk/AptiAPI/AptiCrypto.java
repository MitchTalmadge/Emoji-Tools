package com.AptiTekk.AptiAPI;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

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
        this.keySpec = new SecretKeySpec(this.key.getBytes(StandardCharsets.UTF_8), "AES");
        this.ivSpec = new IvParameterSpec(iv.getBytes());
        this.cipher = Cipher.getInstance("AES/CBC/NoPadding");
    }

    public String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte aData : data) {
                if ((aData & 0xFF) < 16)
                    stringBuilder.append("0").append(Integer.toHexString(aData & 0xFF));
                else
                    stringBuilder.append(Integer.toHexString(aData & 0xFF));
            }
            return stringBuilder.toString();
        }
    }

    public String encrypt(String plainData) throws Exception {

        // Make sure the plainData length should be multiple with 16
        int len = plainData.length();
        int q = len / 16;
        int addSpaces = ((q + 1) * 16) - len;
        StringBuilder stringBuilder = new StringBuilder(plainData);
        for (int i = 0; i < addSpaces; i++) {
            stringBuilder.append(" ");
        }

        this.cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, this.ivSpec);
        byte[] encrypted = cipher.doFinal(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

        return bytesToHex(encrypted);
    }

}
