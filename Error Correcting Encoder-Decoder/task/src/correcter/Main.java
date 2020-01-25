package correcter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
             FileOutputStream output = new FileOutputStream(outputFile)) {
            String text = scanner.nextLine();
            int[][] expand = toExpand(text);
            int[][] parity = toParity(expand);
            String line = toBinaryLine(parity);
            byte[] bytes = toBytes(line);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doSend() {
        File inputFile = new File("encoded.txt");
        File outputFile = new File("received.txt");
        try (FileInputStream input = new FileInputStream(inputFile);
             FileOutputStream output = new FileOutputStream(outputFile)) {
            byte[] bytes = input.readAllBytes();
            String text = fromBytes(bytes);
            String errorMessage = send(text);
            output.write(toBytes(errorMessage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String send(String message) {
        char[] chars = message.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            int index = RANDOM.nextInt(Byte.SIZE);
            for (int j = 0; j < Byte.SIZE; j++) {
                char ch = chars[i + j];
                if (j == index) {
                    builder.append(ch == '1' ? '0' : '1');
                } else {
                    builder.append(ch);
                }
            }
            i += Byte.SIZE - 1;
        }
        return builder.toString();
    }


    private static void doDecode() {
        File inputFile = new File("received.txt");
        File outputFile = new File("decoded.txt");
        try (FileInputStream input = new FileInputStream(inputFile);
             PrintWriter printer = new PrintWriter(new FileOutputStream(outputFile))) {
            byte[] bytes = input.readAllBytes();
            String errorMessage = fromBytes(bytes);
            String decodedMessage = decode(errorMessage);
            out.println("M: " + decodedMessage);
            printer.print(decodedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String decode(String message) {
        StringBuilder builder = new StringBuilder();
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char[] bits = new char[4];
            for (int j = 0; j < Byte.SIZE; j++) {
                char first = chars[i + j++];
                char second = chars[i + j];
                if (first == second) {
                    bits[j / 2] = first;
                } else {
                    bits[j / 2] = 'x';
                }
            }
            if (bits[3] == 'x') {
                for (int j = 0; j < 3; j++) {
                    builder.append(bits[j]);
                }
            } else {
                int count = 0;
                for (int j = 0; j < 4; j++) {
                    if (bits[j] == '1') {
                        ++count;
                    }
                }
                for (int j = 0; j < 3; j++) {
                    if (bits[j] == 'x') {
                        builder.append(count % 2 == 0 ? '0' : '1');
                    } else {
                        builder.append(bits[j]);
                    }
                }
            }
            i += Byte.SIZE - 1;
        }
        StringBuilder target = new StringBuilder();
        char[] charArray = builder.toString().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            StringBuilder charBuilder = new StringBuilder();
            for (int j = 0; j < Byte.SIZE; j++) {
                char ch = charArray[i + j];
                charBuilder.append(ch);
            }
            i += Byte.SIZE - 1;
            int ch = Integer.valueOf(charBuilder.toString(), 2);
            target.append((char) ch);
        }
        return target.toString();
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

    private static String toBinaryLine(int[][] bitArray) {
        StringBuilder builder = new StringBuilder();
        for (int[] bits : bitArray) {
            for (int bit : bits) {
                builder.append(bit);
            }
        }
        return builder.toString();
    }


    private static byte[] toBytes(String binary) {
        char[] chars = binary.toCharArray();
        int length = chars.length;
        int newLength = (length + Byte.SIZE - 1) / Byte.SIZE;
        byte[] target = new byte[newLength];
        int max = Byte.MAX_VALUE + 1;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch == '1') {
                int index = i / Byte.SIZE;
                byte value = (byte) (target[index] | (max >>> (i % Byte.SIZE)));
                target[index] = value;
            }
        }
        return target;
    }

    private static String fromBytes(byte[] bytes) {
        int length = bytes.length * Byte.SIZE;
        StringBuilder builder = new StringBuilder(length);
        int max = Byte.MAX_VALUE + 1;
        for (int i = 0; i < length; i++) {
            int index = i / Byte.SIZE;
            builder.append((bytes[index] << i % Byte.SIZE & max) == 0 ? '0' : '1');
        }
        return builder.toString();
    }
}
