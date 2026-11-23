package mapdemo;

public class MesiSim {
    enum State { INVALID, SHARED, EXCLUSIVE, MODIFIED }

    static final class Core {
        final String name;
        State state = State.INVALID;
        Core(String name) { this.name = name; }
    }

    static void show(String action, Core a, Core b) {
        System.out.println(action);
        System.out.println("  " + a.name + "=" + a.state + "  " + b.name + "=" + b.state);
    }

    public static void main(String[] args) {
        Core c0 = new Core("Core0");
        Core c1 = new Core("Core1");
        show("初始，两个核都没有这行缓存", c0, c1);
        c0.state = State.EXCLUSIVE;
        show("Core0 读，内存把这一行送进来，只有它持有干净副本", c0, c1);
        c0.state = State.SHARED;
        c1.state = State.SHARED;
        show("Core1 也读，两个核都是共享的干净副本", c0, c1);
        c0.state = State.MODIFIED;
        c1.state = State.INVALID;
        show("Core0 写，Core1 的副本作废，Core0 成为唯一的脏副本", c0, c1);
        c0.state = State.SHARED;
        c1.state = State.SHARED;
        show("Core1 再读，Core0 先把脏数据写回，两边重新共享", c0, c1);
    }
}
