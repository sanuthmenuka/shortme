package com.sanuth.shortme.util;

public class Base62Encoder {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String encode(long number) {
        if (number == 0) return "0";

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(CHARACTERS.charAt((int) (number % 62)));
            number /= 62;
        }
        return sb.reverse().toString();
    }

    public static long decode(String encoded) {
        long number = 0;
        for (char c : encoded.toCharArray()) {
            number = number * 62 + CHARACTERS.indexOf(c);
        }
        return number;
    }
}