# Java 框架

框架把对象创建、事务和 HTTP 路由从业务方法里拿走之后，执行顺序由容器决定。这一页用两个小程序把最容易看错的两步跑出来：自调用不经过代理，循环依赖靠早期缓存。

## 自调用不经过代理

源码 [ProxySelfInvocation.java](../demos/src/ProxySelfInvocation.java)，输出 [ProxySelfInvocation.txt](../demos/transcripts/ProxySelfInvocation.txt)。

JDK 动态代理实现的是接口。调用方拿到的是代理对象。代理的 `invoke` 在调用实现对象的方法前后打印“开启事务”和“提交事务”。

调用 `proxy.create("A01")` 时：

1. 调用进代理，打印“代理进入 create，开启事务”。
2. 代理用反射调用 `OrderServiceImpl.create`。
3. `create` 里面写的是 `save(id)`，也就是 `this.save(id)`。`this` 是实现对象，不是代理。
4. `save` 直接执行，不再进入 `invoke`。所以没有第二对“开启事务 / 提交事务”。
5. `create` 返回后，代理才打印“代理离开 create，提交事务”。

```text
调用代理的 create:
代理进入 create，开启事务
  业务 create A01
  业务 save A01
代理离开 create，提交事务
直接调用实现对象的 create:
  业务 create A02
  业务 save A02
```

直接拿实现对象调用 `create` 时，代理完全不在栈上，事务打印一次都没有。Spring 的 `@Transactional` 也是代理。事务注解要生效，调用必须从外面打到代理上。同一个类里的自调用、以及拿到原始对象再调用，都不会经过代理。

对接口的代理常用 JDK 动态代理，对具体类的代理常用生成子类。默认情况下，运行时异常让事务回滚，受检异常不回滚。这个规则可以改，项目里要统一。`REQUIRES_NEW` 会提交内层事务；外层随后失败，内层不会跟着回去。

事务里先做远程调用、再写数据库，远程已经发出去而本地随后回滚，两侧就不一致。远程副作用放到事务成功之后，或做成可以补偿的消息。

## 循环依赖：先暴露未完成的对象

源码 [MiniContainer.java](../demos/src/MiniContainer.java)，输出 [MiniContainer.txt](../demos/transcripts/MiniContainer.txt)。

容器有两个池：`creating` 放已经 `new` 出来但还没注入完的对象，`ready` 放初始化结束的对象。创建 A 时：

1. `new ServiceA()`。A 的字段 `b` 仍是 `null`。立刻放进 `creating`。
2. 给 A 注入 B，于是开始创建 B。
3. `new ServiceB()`，放进 `creating`。
4. B 要注入 A。A 不在 `ready` 里，但在 `creating` 里。容器把这个还没 `init` 的 A 交给 B。
5. B 的 `init` 看见 A 不是 `null`，B 进入 `ready`。
6. 回到 A，字段 `b` 现在有了，A 的 `init` 看见 B，A 进入 `ready`。

```text
开始创建 A
开始创建 B
从早期缓存取出正在创建的 A
B.init 看见 A=true
创建完成 B
A.init 看见 B=true
创建完成 A
```

这只对“先构造、后设字段”成立。若 A 的构造器参数就是 B，B 的构造器参数又是 A，两个对象都还没有被 `new` 出来，早期缓存里没有可以提前给出去的引用。这种环要拆开。

Spring 的单例池和三级缓存做的是同一类事，还要处理工厂对象和 AOP 代理的提前暴露。源码在 `spring-beans` 和 `spring-context`：https://github.com/spring-projects/spring-framework

## 一次 HTTP 请求在框架里的顺序

和 [RequestPath](../demos/src/RequestPath.java) 的第 5 步往后接上：

1. 过滤器处理连接、编码和认证。
2. Dispatcher 用路径和 HTTP 方法选择控制器方法。
3. 参数解析器从路径、查询和正文取出参数。Jackson 把 JSON 变成对象。校验失败就在进业务之前返回。
4. 若方法上有事务代理，先取连接并开始事务，再进业务方法。
5. 返回值交给消息转换器，写成响应正文。
6. 方法正常结束则提交，运行时异常则按规则回滚，然后归还连接。

Boot 的自动配置在类路径上发现某些类时注册默认 Bean。用户自己声明了同类型的 Bean，或少了一个类，条件就不成立，自动配置退开。启动时的条件报告是核对这件事的输出。自动配置代码在：https://github.com/spring-projects/spring-boot 的 `spring-boot-autoconfigure`。

服务框架上的超时和重试只给幂等的方法。Dubbo 的注册、集群和协议在 https://github.com/apache/dubbo 。gRPC 的桩和传输在 https://github.com/grpc/grpc-java 。Tomcat 的连接器和线程池在 https://github.com/apache/tomcat 。JSON 转换在 https://github.com/FasterXML/jackson-databind 。调用失败时的超时、限流和熔断可以套在客户端外面，Resilience4j 把这些做成可组合的装饰器：https://github.com/resilience4j/resilience4j 。
