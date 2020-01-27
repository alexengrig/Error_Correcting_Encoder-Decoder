import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int length = scanner.nextInt();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = scanner.nextInt();
        }
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        for (int i = 1; i < array.length; i++) {
            int prev = array[i - 1];
            int curr = array[i];
            if (prev == n && curr == m || prev == m && curr == n) {
                System.out.println(false);
                return;
            }
        }
        System.out.println(true);
    }
}