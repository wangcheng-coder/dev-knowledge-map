package mapdemo;

public class ClassInitOnce {
    static class Parent {
        static int p = once("2 父类静态初始化");
    }
    static class Child extends Parent {
        static int c = once("3 子类静态初始化");
        static int once(String m) { System.out.println(m); return 1; }
    }
    static int once(String m) { System.out.println(m); return 1; }

    public static void main(String[] args) {
        System.out.println("1 第一次读取 Child.c");
        System.out.println("   值=" + Child.c);
        System.out.println("4 第二次读取 Child.c，静态初始化不会再来");
        System.out.println("   值=" + Child.c);
    }
}
