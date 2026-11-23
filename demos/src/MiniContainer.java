package mapdemo;

import java.util.HashMap;
import java.util.Map;

public class MiniContainer {
    interface Bean {}

    static final class ServiceA implements Bean {
        ServiceB b;
        void init() { System.out.println("A.init 看见 B=" + (b != null)); }
    }

    static final class ServiceB implements Bean {
        ServiceA a;
        void init() { System.out.println("B.init 看见 A=" + (a != null)); }
    }

    private final Map<String, Object> ready = new HashMap<>();
    private final Map<String, Object> creating = new HashMap<>();

    Object get(String name) {
        if (ready.containsKey(name)) {
            System.out.println("从完成池取出 " + name);
            return ready.get(name);
        }
        if (creating.containsKey(name)) {
            System.out.println("从早期缓存取出正在创建的 " + name);
            return creating.get(name);
        }
        System.out.println("开始创建 " + name);
        Object bean = name.equals("A") ? new ServiceA() : new ServiceB();
        creating.put(name, bean);
        if (bean instanceof ServiceA) {
            ((ServiceA) bean).b = (ServiceB) get("B");
            ((ServiceA) bean).init();
        } else {
            ((ServiceB) bean).a = (ServiceA) get("A");
            ((ServiceB) bean).init();
        }
        creating.remove(name);
        ready.put(name, bean);
        System.out.println("创建完成 " + name);
        return bean;
    }

    public static void main(String[] args) {
        new MiniContainer().get("A");
    }
}
