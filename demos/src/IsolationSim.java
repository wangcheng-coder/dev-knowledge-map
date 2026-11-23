package mapdemo;

public class IsolationSim {
    static final class Account {
        int committed = 10;
        Integer pending;
        int dirty() { return pending == null ? committed : pending; }
        void write(int value) { pending = value; }
        void commit() { committed = pending; pending = null; }
        void rollback() { pending = null; }
    }

    public static void main(String[] args) {
        Account account = new Account();
        System.out.println("场景 1  读未提交");
        System.out.println("  已提交余额 " + account.committed);
        account.write(40);
        System.out.println("  T2 把余额改成 40，还没提交。读未提交读到 " + account.dirty());
        account.rollback();
        System.out.println("  T2 回滚。那个 40 从未成为事实，已提交余额回到 " + account.committed);

        System.out.println("场景 2  读已提交，两次普通读取");
        int first = account.committed;
        System.out.println("  T1 第一次读取 " + first);
        account.write(40);
        account.commit();
        int second = account.committed;
        System.out.println("  T2 提交 40 之后，T1 第二次读取 " + second + "。同一事务里两次结果不同，这是不可重复读");

        System.out.println("场景 3  可重复读使用第一次的快照");
        account.committed = 10;
        int snapshot = account.committed;
        account.write(70);
        account.commit();
        System.out.println("  T1 快照仍是 " + snapshot + "，提交后的新值是 " + account.committed);
    }
}
