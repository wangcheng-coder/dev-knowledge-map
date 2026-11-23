package mapdemo;

import java.lang.reflect.Proxy;

public class ProxySelfInvocation {
    interface OrderService {
        void create(String id);
        void save(String id);
    }

    static final class OrderServiceImpl implements OrderService {
        @Override public void create(String id) {
            System.out.println("  业务 create " + id);
            save(id);
        }
        @Override public void save(String id) {
            System.out.println("  业务 save " + id);
        }
    }

    public static void main(String[] args) {
        OrderService impl = new OrderServiceImpl();
        OrderService proxy = (OrderService) Proxy.newProxyInstance(
                OrderService.class.getClassLoader(),
                new Class<?>[] {OrderService.class},
                (target, method, args1) -> {
                    System.out.println("代理进入 " + method.getName() + "，开启事务");
                    Object result = method.invoke(impl, args1);
                    System.out.println("代理离开 " + method.getName() + "，提交事务");
                    return result;
                });
        System.out.println("调用代理的 create:");
        proxy.create("A01");
        System.out.println("直接调用实现对象的 create:");
        impl.create("A02");
    }
}
