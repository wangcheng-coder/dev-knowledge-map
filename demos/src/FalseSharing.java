package mapdemo;

public class FalseSharing {
    public static void main(String[] args) {
        int line = 64;
        int base = 0x1000;
        int a = base + 16;
        int b = a + 8;
        int padded = a + 64;
        System.out.println("缓存行大小 " + line + " 字节。教学地址从 0x1000 对齐到行首。");
        System.out.println(String.format("a 在 0x%X，属于行 0x%X", a, a & ~(line - 1)));
        System.out.println(String.format("紧挨着的 b 在 0x%X，属于行 0x%X", b, b & ~(line - 1)));
        System.out.println("同一行: " + ((a & ~(line - 1)) == (b & ~(line - 1))));
        System.out.println("Core0 写 b 时，整行标成已修改，Core1 上 a 的副本一起作废。");
        System.out.println(String.format("把 b 放到 0x%X 后，属于行 0x%X", padded, padded & ~(line - 1)));
        System.out.println("这时写 b 不再作废 a 所在的那一行。");
    }
}
