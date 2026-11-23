package mapdemo;

public class BytecodeSample {
    static int add(int left, int right) {
        int sum = left + right;
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(add(20, 22));
    }
}
