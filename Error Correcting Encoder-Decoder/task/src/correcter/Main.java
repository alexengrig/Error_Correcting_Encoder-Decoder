package correcter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface Mode {
    String name();

    void execute();
}

public class Main {
    private static Map<String, Mode> MODES;

    static {
        MODES = Stream.of(new EncodeMode(), new SendMode(), new DecodeMode())
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
}

abstract class BaseMode implements Mode {
    protected static final int BYTE_SIZE = 8;
    protected static final int BYTE_OVERFLOW = Byte.MAX_VALUE + 1;

    protected static final char ONE = '1';
    protected static final char ZERO = '0';

    protected static final String DOUBLE_ONES = "11";
    protected static final String DOUBLE_ZEROS = "00";

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

    @Override
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
        int length = (chars.length + BYTE_SIZE - 1) / BYTE_SIZE;
        byte[] bytes = new byte[length];
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ONE) {
                int index = i / BYTE_SIZE;
                int shift = i % BYTE_SIZE;
                byte value = (byte) (bytes[index] | BYTE_OVERFLOW >>> shift);
                bytes[index] = value;
            }
        }
        return bytes;
    }

    protected String fromBytes(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BYTE_SIZE * bytes.length; i++) {
            int index = i / BYTE_SIZE;
            byte value = bytes[index];
            int shift = i % BYTE_SIZE;
            builder.append((value << shift & BYTE_OVERFLOW) == 0 ? ZERO : ONE);
        }
        return builder.toString();
    }

    protected String toHex(int number) {
        return String.format("%2s", Integer.toHexString(number).toUpperCase()).replace(' ', ZERO);
    }

    protected String toHexView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        StringBuilder builder = new StringBuilder();
        char[] chars = binary.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            builder.append(chars[i]);
            if ((i + 1) % BYTE_SIZE == 0) {
                int integer = Integer.valueOf(builder.toString(), 2);
                joiner.add(toHex(integer));
                builder = new StringBuilder();
            }
        }
        return joiner.toString();
    }

    protected String toBinary(int number) {
        int i = number % ((Short.MAX_VALUE + 1) * 2);
        return String.format("%" + BYTE_SIZE + "s", Integer.toBinaryString(i)).replace(' ', ZERO);
    }

    protected String toBinaryView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        StringBuilder builder = new StringBuilder();
        char[] chars = binary.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            builder.append(chars[i]);
            if ((i + 1) % BYTE_SIZE == 0) {
                int integer = Integer.valueOf(builder.toString(), 2);
                joiner.add(toBinary(integer));
                builder = new StringBuilder();
            }
        }
        return joiner.toString();
    }
}

class EncodeMode extends BaseMode {
    protected static final int COUNT_BIT = 4;
    protected static final String DOT = ".";

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

    protected String toHexView(byte[] bytes) {
        StringJoiner joiner = new StringJoiner(" ");
        for (int one : bytes) {
            joiner.add(toHex(one));
        }
        return joiner.toString();
    }

    protected String toExpand(String binary) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, l = binary.length() / COUNT_BIT; i < l; i++) {
            String bits = binary.substring(i * COUNT_BIT, (i + 1) * COUNT_BIT);
            builder.append(DOT)
                    .append(DOT)
                    .append(bits.charAt(0))
                    .append(DOT)
                    .append(bits.charAt(1))
                    .append(bits.charAt(2))
                    .append(bits.charAt(3))
                    .append(DOT);
        }
        return builder.toString();
    }

    protected String toExpandView(String binary) {
        MyStringBuilder builder = new MyStringBuilder(" ");
        for (int i = 0, l = binary.length() / COUNT_BIT; i < l; i++) {
            String bits = binary.substring(i * COUNT_BIT, (i + 1) * COUNT_BIT);
            builder.append(DOT)
                    .append(DOT)
                    .append(bits.charAt(0))
                    .append(DOT)
                    .append(bits.charAt(1))
                    .append(bits.charAt(2))
                    .append(bits.charAt(3))
                    .append(DOT)
                    .delimit();
        }
        return builder.toString();
    }

    protected String toParity(String binary) {
        MyStringBuilder builder = new MyStringBuilder();
        for (int i = 0, l = binary.length() / Byte.SIZE; i < l; i++) {
            String bits = binary.substring(i * Byte.SIZE, (i + 1) * Byte.SIZE);
            int countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(2));
            countOnes = 0;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(4))
                    .append(bits.charAt(5))
                    .append(bits.charAt(6))
                    .append(ZERO)
                    .delimit();
        }
        return builder.toString();
    }

    protected String toParityView(String binary) {
        MyStringBuilder builder = new MyStringBuilder(" ");
        for (int i = 0, l = binary.length() / Byte.SIZE; i < l; i++) {
            String bits = binary.substring(i * Byte.SIZE, (i + 1) * Byte.SIZE);
            int countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(2));
            countOnes = 0;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(4))
                    .append(bits.charAt(5))
                    .append(bits.charAt(6))
                    .append(ZERO)
                    .delimit();
        }
        return builder.toString();
    }

    protected String toParityHexView(String binary) {
        MyStringBuilder builder = new MyStringBuilder(" ");
        for (int i = 0, l = binary.length() / Byte.SIZE; i < l; i++) {
            String bits = binary.substring(i * Byte.SIZE, (i + 1) * Byte.SIZE);
            int countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            countOnes = 0;
            if (bits.charAt(2) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(2));
            countOnes = 0;
            if (bits.charAt(4) == ONE) ++countOnes;
            if (bits.charAt(5) == ONE) ++countOnes;
            if (bits.charAt(6) == ONE) ++countOnes;
            if (countOnes % 2 == 0) {
                builder.append(ZERO);
            } else {
                builder.append(ONE);
            }
            builder.append(bits.charAt(4))
                    .append(bits.charAt(5))
                    .append(bits.charAt(6))
                    .append(ZERO)
                    .delimit(v -> toHex(Integer.valueOf(v, 2)));
        }
        return builder.toString();
    }
}

class SendMode extends BaseMode {
    protected SendMode() {
        super("send", "encoded.txt", "received.txt");
    }

    @Override
    protected byte[] execute(byte[] bytes) {
        System.out.println(inputFilename + ":");
        String binary = fromBytes(bytes);
        String hexTextView = toHexView(binary);
        System.out.println("hex view: " + hexTextView);
        String binaryTextView = toBinaryView(binary);
        System.out.println("bin view: " + binaryTextView);
        System.out.println();
        System.out.println(outputFilename + ":");
        String errorView = toErrorView(binary);
        System.out.println("bin view: " + errorView);
        String errorHexView = toErrorHexView(binary);
        System.out.println("hex view: " + errorHexView);
        String error = toError(binary);
        return toBytes(error);
    }

    protected String toError(String binary) {
        StringBuilder builder = new StringBuilder();
        char[] charArray = binary.toCharArray();
        Random random = new Random();
        for (int i = 0, size = BYTE_SIZE, l = charArray.length; i < l; ) {
            int errorIndex = random.nextInt(BYTE_SIZE);
            for (int j = 0, index = j + i; j < size && index < l; index = ++j + i) {
                char ch = charArray[index];
                if (j != errorIndex) {
                    builder.append(ch);
                } else {
                    builder.append(ch == ONE ? ZERO : ONE);
                }
            }
            i += size;
        }
        return builder.toString();
    }

    protected String toErrorView(String binary) {
        MyStringBuilder builder = new MyStringBuilder(" ");
        char[] charArray = binary.toCharArray();
        Random random = new Random();
        for (int i = 0, size = BYTE_SIZE, l = charArray.length; i < l; ) {
            int errorIndex = random.nextInt(BYTE_SIZE);
            for (int j = 0, index = j + i; j < size && index < l; index = ++j + i) {
                char ch = charArray[index];
                if (j != errorIndex) {
                    builder.append(ch);
                } else {
                    builder.append(ch == ONE ? ZERO : ONE);
                }
            }
            builder.delimit();
            i += size;
        }
        return builder.toString();
    }

    protected String toErrorHexView(String binary) {
        MyStringBuilder builder = new MyStringBuilder(" ");
        char[] charArray = binary.toCharArray();
        Random random = new Random();
        for (int i = 0, size = BYTE_SIZE, l = charArray.length; i < l; ) {
            int errorIndex = random.nextInt(BYTE_SIZE);
            for (int j = 0, index = j + i; j < size && index < l; index = ++j + i) {
                char ch = charArray[index];
                if (j != errorIndex) {
                    builder.append(ch);
                } else {
                    builder.append(ch == ONE ? ZERO : ONE);
                }
            }
            builder.delimit(v -> toHex(Integer.valueOf(v, 2)));
            i += size;
        }
        return builder.toString();
    }
}

class DecodeMode extends BaseMode {
    protected static final int BIT_COUNT = 6;

    protected DecodeMode() {
        super("decode", "received.txt", "decoded.txt");
    }

    @Override
    protected byte[] execute(byte[] bytes) {
        System.out.println(inputFilename + ":");
        String binary = fromBytes(bytes);
        String hexTextView = toHexView(binary);
        System.out.println("hex view: " + hexTextView);
        String binaryTextView = toBinaryView(binary);
        System.out.println("bin view: " + binaryTextView);
        System.out.println();
        System.out.println(outputFilename + ":");
        String correctView = toCorrectView(binary);
        System.out.println("correct: " + correctView);
        String correct = toCorrect(binary);
        String decodeView = toDecodeView(correct);
        System.out.println("decode: " + decodeView);
        String decode = toDecode(correct);
        String remove = toRemove(decode);
        String removeView = toBinaryView(remove);
        System.out.println("remove: " + removeView);
        String removeHexView = toHexView(remove);
        System.out.println("hex view: " + removeHexView);
        byte[] target = toBytes(remove);
        String text = new String(target);
        System.out.println("text view: " + text);
        return target;
    }

    protected String toCorrect(String binary) {
        StringBuilder builder = new StringBuilder();
        char[] charArray = binary.toCharArray();
        String x = "xx";
        for (int i = 0, size = BYTE_SIZE, l = charArray.length; i < l; ) {
            StringBuilder subbuilder = new StringBuilder();
            int countOne = 0;
            for (int j = 1, index = j + i; j < size && index < l; j += 2, index = j + i) {
                char prev = charArray[index - 1];
                char curr = charArray[index];
                if (prev != curr) {
                    subbuilder.append(x);
                } else {
                    subbuilder.append(prev).append(curr);
                    if (prev == ONE) {
                        ++countOne;
                    }
                }
            }
            String bits = subbuilder.toString();
            if (countOne % 2 == 0) {
                builder.append(bits.replace(x, DOUBLE_ZEROS));
            } else {
                builder.append(bits.replace(x, DOUBLE_ONES));
            }
            i += size;
        }
        return builder.toString();
    }

    protected String toCorrectView(String binary) {
        StringJoiner joiner = new StringJoiner(" ");
        char[] charArray = binary.toCharArray();
        String x = "xx";
        for (int i = 0, size = BYTE_SIZE, l = charArray.length; i < l; ) {
            StringBuilder builder = new StringBuilder();
            int countOne = 0;
            for (int j = 1, index = j + i; j < size && index < l; j += 2, index = j + i) {
                char prev = charArray[index - 1];
                char curr = charArray[index];
                if (prev != curr) {
                    builder.append(x);
                } else {
                    builder.append(prev).append(curr);
                    if (prev == ONE) {
                        ++countOne;
                    }
                }
            }
            String bits = builder.toString();
            if (countOne % 2 == 0) {
                joiner.add(bits.replace(x, DOUBLE_ZEROS));
            } else {
                joiner.add(bits.replace(x, DOUBLE_ONES));
            }
            i += size;
        }
        return joiner.toString();
    }

    protected String toDecode(String binary) {
        char[] charArray = binary.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0, l = charArray.length; i < l; ) {
            for (int j = 1, index = j + i; j < BIT_COUNT && index < l; j += 2, index = j + i) {
                char ch = charArray[index];
                builder.append(ch);
            }
            i += BYTE_SIZE;
        }
        return builder.toString();
    }

    protected String toDecodeView(String binary) {
        char[] charArray = binary.toCharArray();
        StringJoiner joiner = new StringJoiner(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0, l = charArray.length; i < l; ) {
            for (int j = 1, index = j + i; j < BIT_COUNT && index < l; j += 2, index = j + i) {
                char ch = charArray[index];
                builder.append(ch);
                if (builder.toString().length() == BYTE_SIZE) {
                    joiner.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
            i += BYTE_SIZE;
        }
        return joiner.add(builder.toString()).toString();
    }

    protected String toRemove(String binary) {
        int countByte = binary.length() / BYTE_SIZE;
        return binary.substring(0, countByte * BYTE_SIZE);
    }
}

class MyStringBuilder {
    private final StringJoiner joiner;

    private StringBuilder builder;

    MyStringBuilder() {
        this("");
    }

    MyStringBuilder(String delimiter) {
        this.joiner = new StringJoiner(delimiter);
        this.builder = new StringBuilder();
    }

    MyStringBuilder append(String text) {
        builder.append(text);
        return this;
    }

    public MyStringBuilder append(char ch) {
        builder.append(ch);
        return this;
    }

    MyStringBuilder delimit() {
        String text = builder.toString();
        if (!text.isEmpty()) {
            joiner.add(text);
            builder = new StringBuilder();
        }
        return this;
    }

    MyStringBuilder delimit(Function<String, String> mapper) {
        String text = builder.toString();
        if (!text.isEmpty()) {
            joiner.add(mapper.apply(text));
            builder = new StringBuilder();
        }
        return this;
    }

    @Override
    public String toString() {
        return this.joiner.toString();
    }
}