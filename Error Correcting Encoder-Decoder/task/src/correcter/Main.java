package correcter;

import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String ALPHABET;
    private static final Random RANDOM = new Random();

    static {
        StringBuilder builder = new StringBuilder();
        for (char i = 'A'; i <= 'Z'; i++) {
            builder.append(i);
        }
        for (char i = 'a'; i <= 'z'; i++) {
            builder.append(i);
        }
        for (char i = '0'; i <= '9'; i++) {
            builder.append(i);
        }
        builder.append(' ');
        ALPHABET = builder.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String word = scanner.nextLine();
        char[] chars = word.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (i % 3 == 0) {
                builder.append(getRandom());
            } else {
                builder.append(ch);
            }
        }
        System.out.println(builder);
    }

    private static char getRandom() {
        return ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
    }
}
