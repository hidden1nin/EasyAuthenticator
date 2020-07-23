package com.runicrealms.runicauthenticator.runicauthenticator;


import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KeyHandler {
    public static String getQRBarcodeURL(
            String user,
            String host,
            String secret) {
        String format = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
        return String.format(format, user, host, secret);
    }
    public static byte[] toByte(String key){
        return key.getBytes();
    }
    public static int backup_code(
            byte[] key,
            long number)
            throws NoSuchAlgorithmException,
            InvalidKeyException {
        byte[] data = new byte[8];
        long value = number;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }


        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);


        int offset = hash[20 - 1] & 0xF;

        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }


        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;


        return (int) truncatedHash;
    }
}
