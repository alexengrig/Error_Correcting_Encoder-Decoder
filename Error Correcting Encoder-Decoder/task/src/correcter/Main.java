package correcter;

import java.io.*;
import java.util.*;

import static java.lang.System.out;

public class Main {
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String mode = scanner.nextLine();
        out.println("Write a mode: " + mode);
        out.println();
        if ("encode".equals(mode)) {
            doEncode();
        } else if ("send".equals(mode)) {
            doSend();
        } else if ("decode".equals(mode)) {
            doDecode();
        } else {
            throw new IllegalArgumentException("Unknown mode");
        }
    }


    private static void doEncode() {
        String inputFilename = "send.txt";
        File inputFile = new File(inputFilename);
        String outputFilename = "encoded.txt";
        File outputFile = new File(outputFilename);
        try (Scanner scanner = new Scanner(new FileInputStream(inputFile));
             PrintWriter printer = new PrintWriter(new FileOutputStream(outputFile))) {
            out.println(inputFilename + ":");
            String text = scanner.nextLine();
            out.println("text view: " + text);
            out.println("hex view: " + toHexView(text));
            out.println("bin view: " + toBinaryView(text));
            out.println();
            out.println(outputFilename + ":");
            int[][] expand = toExpand(text);
            out.println("expand: " + toExpandView(expand));
            int[][] parity = toParity(expand);
            out.println("parity: " + toBinaryView(parity));
            String encodedText = toHexView(parity);
            out.println("hex view: " + encodedText);
            printer.print(encodedText);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String encode(int[][] octalArray) {
        StringBuilder builder = new StringBuilder();
        for (int[] octal : octalArray) {
            int result = 0;
            for (int bit : octal) {
                result <<= 1;
                result |= bit;
            }
            builder.append((char) result);
        }
        return builder.toString();
    }

    private static void doSend() {
        File inputFile = new File("encoded.txt");
        File outputFile = new File("received.txt");
        try (Scanner scanner = new Scanner(new FileInputStream(inputFile));
             PrintWriter printer = new PrintWriter(new FileOutputStream(outputFile))) {
            String message = scanner.nextLine();
            String decodedMessage = send(message);
            printer.print(decodedMessage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String send(String message) {
        char[] chars = message.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int ch : chars) {
            builder.append((char) (ch ^ (1 << RANDOM.nextInt(7))));
        }
        return builder.toString();
    }


    private static void doDecode() {
        File inputFile = new File("received.txt");
        File outputFile = new File("decoded.txt");
        try (Scanner scanner = new Scanner(new FileInputStream(inputFile));
             PrintWriter printer = new PrintWriter(new FileOutputStream(outputFile))) {
            String message = scanner.nextLine();
            String decodedMessage = decode(message);
            printer.print(decodedMessage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String decode(String message) {
        char[] chars = message.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int ch : chars) {
            builder.append((char) (ch ^ (1 << RANDOM.nextInt(7))));
        }
        return builder.toString();
    }


    private static int[] toBits(String text) {
        char[] chars = text.toCharArray();
        List<Integer> bytes = new ArrayList<>();
        for (int ch : chars) {
            for (char bit : toBinaryString(ch).toCharArray()) {
                bytes.add(bit == '1' ? 1 : 0);
            }
        }
        return bytes.stream().mapToInt(Integer::valueOf).toArray();
    }

    private static int[][] toTriple(int[] bits) {
        return slice(bits, 3);
    }

    private static int[][] toOctal(int[] bits) {
        return slice(bits, 8);
    }


    private static int[][] slice(int[] bits, int size) {
        if (bits.length % size == 0) {
            return sliceEqually(bits, size);
        }
        int length = bits.length;
        int[][] target = new int[(length / size) + 1][];
        int lastArrayIndex = target.length - 1;
        for (int i = 0; i < lastArrayIndex; i++) {
            target[i] = new int[size];
        }
        target[lastArrayIndex] = new int[length % size];
        for (int i = 0; i < length - 1; i++) {
            target[i / size][i % size] = bits[i];
        }
        return target;
    }

    private static int[][] sliceEqually(int[] bits, int size) {
        int length = bits.length;
        int[][] target = new int[length / size][size];
        for (int i = 0; i < length; i++) {
            target[i / size][i % size] = bits[i];
        }
        return target;
    }


    private static int[][] toExpand(String text) {
        int[] bits = toBits(text);
        int[][] tripleArray = toTriple(bits);
        int length = tripleArray.length;
        int lastLength = tripleArray[length - 1].length;
        int size = 6;
        int[][] targetArray;
        if (lastLength == size / 2) {
            targetArray = new int[length][size];
        } else {
            targetArray = new int[length][];
            for (int i = 0; i < length - 1; i++) {
                targetArray[i] = new int[size];
            }
            targetArray[length - 1] = new int[lastLength * 2];
        }
        for (int i = 0; i < length; i++) {
            for (int j = 0, l = tripleArray[i].length, k = 0; j < l; j++) {
                int bit = tripleArray[i][j];
                targetArray[i][k++] = bit;
                targetArray[i][k++] = bit;
            }
        }
        return targetArray;
    }

    private static int[][] toParity(int[][] expand) {
        int length = expand.length;
        int extraSize = 2;
        int size = expand[0].length + extraSize;
        int[][] targetArray = new int[length][size];
        for (int i = 0; i < length; i++) {
            int[] bits = expand[i];
            int[] target = targetArray[i];
            int firstBit = bits[0];
            target[0] = firstBit;
            int xor = firstBit;
            for (int j = 1; j < bits.length; j++) {
                int bit = bits[j];
                target[j] = bit;
                if (j % 2 == 0) {
                    xor ^= bit;
                }
            }
            for (int j = size - extraSize; j < size; j++) {
                target[j] = xor;
            }
        }
        return targetArray;
    }


    private static String toBinaryString(int ch) {
        return String.format("%8s", Integer.toBinaryString(ch)).replace(' ', '0');
    }

    private static String toHexString(int ch) {
        return String.format("%2s", Integer.toHexString(ch)).replace(' ', '0').toUpperCase();
    }

    private static String toHexView(String text) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int ch : text.toCharArray()) {
            joiner.add(toHexString(ch));
        }
        return joiner.toString();
    }

    private static String toHexView(int[][] bitArray) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int[] bits : bitArray) {
            StringBuilder builder = new StringBuilder();
            for (int bit : bits) {
                builder.append(bit);
            }
            Integer value = Integer.valueOf(builder.toString(), 2);
            joiner.add(toHexString(value));
        }
        return joiner.toString();
    }

    private static String toBinaryView(String text) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int ch : text.toCharArray()) {
            joiner.add(toBinaryString(ch));
        }
        return joiner.toString();
    }

    private static String toBinaryView(int[][] bitArray) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int[] bits : bitArray) {
            StringBuilder builder = new StringBuilder();
            for (int bit : bits) {
                builder.append(bit);
            }
            joiner.add(builder);
        }
        return joiner.toString();
    }


    private static String toExpandView(int[][] bitArray) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int[] bits : bitArray) {
            StringBuilder builder = new StringBuilder();
            for (int bit : bits) {
                builder.append(bit);
            }
            joiner.add(builder.append(".".repeat(8 - bits.length)));
        }
        return joiner.toString();
    }

}
