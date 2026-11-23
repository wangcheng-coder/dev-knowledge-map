package mapdemo;

public class TryWithResources {
    static final class Res implements AutoCloseable {
        final String name;
        Res(String name) {
            this.name = name;
            System.out.println("打开 " + name);
        }
        void work() {
            if (name.equals("B")) {
                throw new IllegalStateException("业务在 B 上失败");
            }
        }
        @Override public void close() {
            System.out.println("关闭 " + name);
            if (name.equals("A")) {
                throw new IllegalStateException("关闭 A 失败");
            }
        }
    }

    public static void main(String[] args) {
        try (Res a = new Res("A"); Res b = new Res("B")) {
            b.work();
        } catch (IllegalStateException ex) {
            System.out.println("主异常: " + ex.getMessage());
            for (Throwable s : ex.getSuppressed()) {
                System.out.println("被压制的关闭异常: " + s.getMessage());
            }
        }
    }
}
