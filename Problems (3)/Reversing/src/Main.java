import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int number = scanner.nextInt();
        if (number < 100 || number > 999) {
            throw new IllegalArgumentException();
        }
        int a = number / 100;
        int b = (number % 100) / 10;
        int c = number % 10;
        System.out.println("" + c + b + a);
    }
}