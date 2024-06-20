package com.example.dreammusic;

import java.security.SecureRandom;

public class RandomUniqueIdGenerator {

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateRandomUniqueId(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String randomUniqueId = generateRandomUniqueId(6);
        System.out.println(randomUniqueId);
    }
}

