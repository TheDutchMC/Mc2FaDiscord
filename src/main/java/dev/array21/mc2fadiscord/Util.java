package dev.array21.mc2fadiscord;

import java.util.Random;

public class Util {

    private static final Random rnd = new Random();
    public static String generateCode(final int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append((char)('0' + rnd.nextInt(10)));
        }
        return sb.toString();
    }
}
