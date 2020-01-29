package correcter;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface Mode {
    String name();

    void execute();
}

public class Main {
    private static final Random RANDOM = new Random();
    private static Map<String, Mode> MODES;

    static {
        MODES = Stream.of(new EncodeMode())
                .collect(Collectors.toMap(Mode::name, Function.identity()));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String modeName = scanner.nextLine();
        System.out.println("Write a mode: " + modeName);
        System.out.println();
        Mode mode = MODES.get(modeName);
        if (mode == null) {
            throw new IllegalArgumentException("Unknown mode");
        }
        mode.execute();
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

abstract class BaseMode implements Mode {
    protected final String name;
    protected final String inputFilename;
    protected final String outputFilename;

    protected BaseMode(String name, String inputFilename, String outputFilename) {
        this.name = name;
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    @Override
    public String name() {
        return name;
    }

    public void execute1() {
        execute("Test".getBytes());
    }

    public void execute() {
        final File inputFile = new File(inputFilename);
        final File outputFile = new File(outputFilename);
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] inputBytes = inputStream.readAllBytes();
            byte[] outputBytes = execute(inputBytes);
            outputStream.write(outputBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract byte[] execute(byte[] bytes);

    protected byte[] toBytes(String binary) {
        char[] chars = binary.toCharArray();
        int length = (chars.length + Byte.SIZE - 1) / Byte.SIZE;
        byte[] bytes = new byte[length];
        int max = Byte.MAX_VALUE + 1;
        for (int i = 0; i < chars.length; i++) {
            int index = i / Byte.SIZE;
            int shift = i % Byte.SIZE;
            byte value = (byte) (bytes[index] | max >>> shift);
            bytes[index] = value;
        }
        return bytes;
    }


    protected String toHex(int number) {
        return String.format("%2s", Integer.toHexString(number).toUpperCase()).replace(" ", "0");
    }

    protected String toHexView(byte[] bytes) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int one : bytes) {
            joiner.add(toHex(one));
        }
        return joiner.toString();
    }


    protected String toBinary(int number) {
        int i = number % ((Short.MAX_VALUE + 1) * 2);
        return String.format("%" + Byte.SIZE + "s", Integer.toBinaryString(i)).replace(" ", "0");
    }

    protected String toBinary(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int one : bytes) {
            builder.append(toBinary(one));
        }
        return builder.toString();
    }

    protected String toBinaryView(byte[] bytes) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int one : bytes) {
            joiner.add(toBinary(one));
        }
        return joiner.toString();
    }


    protected String toExpand(String binary) {
        StringBuilder builder = new StringBuilder();
        char[] chars = binary.toCharArray();
        for (char ch : chars) {
            builder.append(ch).append(ch);
        }
        return builder.toString();
    }

    protected String toExpandView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        char[] charArray = binary.toCharArray();
        int three = 3;
        for (int i = 0, size = three, l = charArray.length; i < l; ) {
            StringBuilder builder = new StringBuilder();
            int count = 0;
            for (int j = count + i; count < size && j < l; j = ++count + i) {
                char ch = charArray[j];
                builder.append(ch).append(ch);
            }
            int repeat = Byte.SIZE - (size * 2 - ((size - count) * 2));
            joiner.add(builder.append(".".repeat(repeat)).toString());
            i += size;
        }
        return joiner.toString();
    }


    protected String toParity(String binary) {
        StringBuilder builder = new StringBuilder();
        char[] charArray = binary.toCharArray();
        int three = 3;
        int two = 2;
        for (int i = 0, size = three * two, l = charArray.length; i < l; ) {
            int count = 0;
            int countOne = 0;
            for (int j = count + i; count < size && j < l; j = ++count + i) {
                char ch = charArray[j];
                builder.append(ch);
                if (ch == '1') {
                    ++countOne;
                }
            }
            builder.append("0".repeat(size - count));
            builder.append(countOne % 2 == 0 ? "00" : "11");
            i += size;
        }
        return builder.toString();
    }

    protected String toParityView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        char[] charArray = binary.toCharArray();
        int three = 3;
        int two = 2;
        for (int i = 0, size = three * two, l = charArray.length; i < l; ) {
            StringBuilder builder = new StringBuilder();
            int count = 0;
            int countOne = 0;
            for (int j = count + i; count < size && j < l; j = ++count + i) {
                char ch = charArray[j];
                builder.append(ch);
                if (ch == '1') {
                    ++countOne;
                }
            }
            builder.append("0".repeat(size - count));
            builder.append((countOne / two) % 2 == 0 ? "00" : "11");
            joiner.add(builder);
            i += size;
        }
        return joiner.toString();
    }

    protected String toParityHexView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        char[] charArray = binary.toCharArray();
        int three = 3;
        int two = 2;
        for (int i = 0, size = three * two, l = charArray.length; i < l; ) {
            StringBuilder builder = new StringBuilder();
            int count = 0;
            int countOne = 0;
            for (int j = count + i; count < size && j < l; j = ++count + i) {
                char ch = charArray[j];
                builder.append(ch);
                if (ch == '1') {
                    ++countOne;
                }
            }
            builder.append("0".repeat(size - count));
            builder.append((countOne / two) % 2 == 0 ? "00" : "11");
            int integer = Integer.valueOf(builder.toString(), 2);
            joiner.add(toHex(integer));
            i += size;
        }
        return joiner.toString();
    }
}

class EncodeMode extends BaseMode {
    protected EncodeMode() {
        super("encode", "send.txt", "encoded.txt");
    }

    @Override
    protected byte[] execute(byte[] bytes) {
        String text = new String(bytes);
        System.out.println(inputFilename + ":");
        System.out.println("text view: " + text);
        String hexTextView = toHexView(bytes);
        System.out.println("hex view: " + hexTextView);
        String binaryTextView = toBinaryView(bytes);
        System.out.println("bin view: " + binaryTextView);
        System.out.println();
        System.out.println(outputFilename + ":");
        String binary = toBinary(bytes);
        String expandView = toExpandView(binary);
        System.out.println("expand: " + expandView);
        String expand = toExpand(binary);
        String parityView = toParityView(expand);
        System.out.println("parity: " + parityView);
        String parityHexView = toParityHexView(expand);
        System.out.println("hex view: " + parityHexView);
        String parity = toParity(expand);
        return toBytes(parity);
    }
}