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
        String message = scanner.nextLine();
        System.out.println(message);
        char[] chars = message.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char ch : chars) {
            builder.append(String.valueOf(ch).repeat(3));
        }
        String encodedMessage = builder.toString();
        System.out.println(encodedMessage);
        chars = encodedMessage.toCharArray();
        builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            int index = RANDOM.nextInt(3);
            for (int j = 0; j < 3; j++, i++) {
                char ch = chars[i];
                if (j != index) {
                    builder.append(ch);
                } else {
                    builder.append(getRandom());
                }
            }
            --i;
        }
        String errorMessage = builder.toString();
        System.out.println(errorMessage);
        chars = errorMessage.toCharArray();
        builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char[] triple = new char[3];
            for (int j = 0; j < 3; j++, i++) {
                triple[j] = chars[i];
            }
            --i;
            if (triple[0] == triple[1]) {
                builder.append(triple[0]);
            } else if (triple[1] == triple[2]) {
                builder.append(triple[1]);
            } else if (triple[2] == triple[0]) {
                builder.append(triple[2]);
            }
        }
        System.out.println(builder);
    }

    private static char getRandom() {
        return ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
    }
}
