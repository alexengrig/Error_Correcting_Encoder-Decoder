package correcter;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(new FileInputStream(new File("send.txt")));
             PrintWriter printer = new PrintWriter(new FileOutputStream(new File("received.txt")))) {
            String message = scanner.nextLine();
            char[] chars = message.toCharArray();
            for (int ch : chars) {
                printer.print((char) (ch ^ (1 << RANDOM.nextInt(7))));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
