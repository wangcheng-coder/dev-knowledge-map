package mapdemo;

public class PassByValue {
    static class Bag {
        int n;
        Bag(int n) { this.n = n; }
    }

    static void change(int n, Bag bag, Bag slot) {
        System.out.println("方法内一开始: n=" + n + " bag.n=" + bag.n + " slot和bag是同一对象: " + (slot == bag));
        n = 100;
        bag.n = 100;
        slot = new Bag(7);
        System.out.println("方法内改完: n=" + n + " bag.n=" + bag.n + " 新slot.n=" + slot.n);
    }

    public static void main(String[] args) {
        int n = 1;
        Bag bag = new Bag(1);
        System.out.println("调用前: n=" + n + " bag.n=" + bag.n + " 对象身份=" + System.identityHashCode(bag));
        change(n, bag, bag);
        System.out.println("调用后: n=" + n + " bag.n=" + bag.n + " 对象身份=" + System.identityHashCode(bag));
    }
}
