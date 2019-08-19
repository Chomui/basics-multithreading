package com.artemchep.basics_multithreading.domain;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Do not modify this file
public class Message {

    @NonNull
    public final String key;

    @NonNull
    public final String plainText;

    @Nullable
    public final String cipherText;

    public Message(String key, String plainText) {
        this(key, plainText, null);
    }

    public Message(@NonNull String key, @NonNull String plainText, @Nullable String cipherText) {
        this.key = key;
        this.plainText = plainText;
        this.cipherText = cipherText;
    }

    public Message copy(@Nullable String cipherText) {
        return new Message(key, plainText, cipherText);
    }

    public static Message generate() {
        String key = String.valueOf(SystemClock.elapsedRealtimeNanos());
        String plainText = String.valueOf(SystemClock.elapsedRealtimeNanos());
        return new Message(key, plainText);
    }

}
