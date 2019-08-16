package com.artemchep.basics_multithreading.cipher;

public class CipherUtil {

    public static long WORK_MILLIS = 500L;

    public static String encrypt(String plainText) {
        // Simulates the real struggle of encryption.
        try {
            Thread.sleep(WORK_MILLIS * (int) (Math.random() * 6));
        } catch (InterruptedException ignored) {
        }

        return String.valueOf(plainText.hashCode()); // yes, this is not a real encryption method
    }

}
