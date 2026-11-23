package mapdemo;

public class InitOrder {
    static class Parent {
        Parent() {
            System.out.println("3 父类构造器开始，马上调用可覆盖的 greet()");
            greet();
            System.out.println("5 父类构造器结束");
        }
        void greet() {
            System.out.println("4 父类 greet");
        }
    }

    static class Child extends Parent {
        String name = assign();
        static String assign() {
            System.out.println("6 子类字段 name 初始化");
            return "ready";
        }
        Child() {
            System.out.println("7 子类构造器，name=" + name);
        }
        @Override
        void greet() {
            System.out.println("4 子类 greet 被父类构造器调用，此时 name=" + name);
        }
    }

    public static void main(String[] args) {
        System.out.println("1 准备 new Child()");
        Child c = new Child();
        System.out.println("8 创建完成，name=" + c.name);
    }
}
