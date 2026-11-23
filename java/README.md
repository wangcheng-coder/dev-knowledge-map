# Java

Java 开发要同时看懂四层：语言写出的结构，虚拟机如何执行和回收，并发下内存的可见性，以及框架在这些机制上加的代理、连接池和线程模型。下层的硬件、内核和网络决定了这些机制为什么长这样，入口在仓库根目录的 [总览](../README.md)。

## 建议顺序

1. [语言与类型](language.md)：值如何传递，类型在编译后还剩什么，资源如何用 try-with-resources 关上。
2. [JVM](jvm.md)：类如何被加载，对象放在哪，GC 和 JIT 各自改变什么。
3. [并发](concurrency.md)：先建立 happens-before，再使用线程池和并发容器。
4. [I/O 与网络](io-and-network.md)：阻塞、多路复用、Netty、零拷贝。
5. [数据访问](data.md)：JDBC、事务、缓存和消息消费。
6. [框架](frameworks.md)：Spring 容器、事务代理、Boot 自动配置、Dubbo 与 gRPC。
7. [工程化](engineering.md)：构建、测试、日志和指标。
8. [项目索引](projects.md)：按主题汇总跳转。

## 日常排障时先对哪一页

| 看到的现象 | 先读 |
| --- | --- |
| 启动很慢、类找不到、依赖冲突 | 语言里的类路径，JVM 的类加载，工程化里的依赖调解 |
| CPU 高、停顿、内存涨 | JVM 的 GC 与 JIT，并发里的线程模型 |
| 接口超时、连接被重置 | I/O 与网络，以及总览里的 TCP |
| 数据重复、读到旧值、事务没回滚 | 数据访问，框架里的事务代理 |
| 本地方法崩溃 | 运行时页的调用约定，以及 JNI 边界 |

## 源码怎么读

优先读 OpenJDK 和 Spring 的当前主干，社区笔记用来找关键词。每个大仓库只先进入表格里写的目录，把一条路径读通，再横向扩展。

| 想确认的事 | 仓库 | 地址 |
| --- | --- | --- |
| 语言和标准库的定义实现 | OpenJDK | https://github.com/openjdk/jdk |
| 对象布局 | JOL | https://github.com/openjdk/jol |
| 微基准应该怎么写 | JMH | https://github.com/openjdk/jmh |
| 容器、事务、MVC | Spring Framework | https://github.com/spring-projects/spring-framework |
| 应用启动和自动配置 | Spring Boot | https://github.com/spring-projects/spring-boot |
