# 开发知识地图

从晶体管到一条 Java 业务请求，把开发要掌握的机制按层收在一起。每一层只做两件事：讲清这一层在解决什么，并跳到能直接打开的 GitHub 项目。

这份仓库是地图和入口，用来定位「该懂哪一段底层、该读哪份源码」。具体 API 和版本行为以对应项目的官方文档和当前代码为准。

## 一条请求怎样走完

下面这条路径把各层串起来。Java 开发平时写的是最后几步，性能、故障和边界行为经常落在前面几步。

1. 网卡把报文用 DMA 写入内存，再用中断告诉 CPU。CPU 不必自己把每个字节从设备寄存器里搬出来。
2. 内核协议栈把帧解析成 IP、TCP，套接字缓冲区里出现可读数据。
3. 进程通过系统调用或 `epoll` 被唤醒。这一步跨过用户态和内核态。
4. JVM 的 NIO 或 Netty 的事件循环把字节放进 `ByteBuffer`。
5. Spring MVC 按路由找到控制器。事务代理在业务方法前后打开和提交数据库事务。
6. JDBC 把 SQL 交给数据库。数据库用自己的缓冲池和预写日志把页写到磁盘。页缓存和文件系统决定这次写到底什么时候落盘。

## 层

| 层 | 这一层决定什么 | 入口 |
| --- | --- | --- |
| 硬件 | 指令怎么执行，数据放在哪一级存储，设备怎么通知 CPU | [hardware](hardware/README.md) |
| 固件与软硬结合 | 机器如何启动，驱动如何认设备，实时系统如何调度 | [firmware](firmware/README.md) |
| 操作系统 | 进程、虚拟内存、文件、锁如何被内核实现 | [os](os/README.md) |
| 网络 | 连接、重传、拥塞和事件驱动 I/O 如何工作 | [network](network/README.md) |
| 语言运行时 | 源码如何变成进程里正在跑的代码，内存谁来回收 | [runtime](runtime/README.md) |
| Java | 语言、JVM、并发、I/O、数据访问、框架和工程化 | [java](java/README.md) |

建议先读本页，再按表从上往下各读一遍。日常 Java 工作从 [java/README.md](java/README.md) 进入，遇到说不清的停顿或数据错误时，回到对应的下层。

## Java 主线

Java 内容按使用顺序排，源码入口写在各页表格里。

| 主题 | 页 |
| --- | --- |
| 学习顺序 | [java/README.md](java/README.md) |
| 语言与类型 | [java/language.md](java/language.md) |
| JVM：类加载、内存、GC、JIT | [java/jvm.md](java/jvm.md) |
| 并发与内存模型 | [java/concurrency.md](java/concurrency.md) |
| I/O、Netty、零拷贝 | [java/io-and-network.md](java/io-and-network.md) |
| JDBC、缓存、消息 | [java/data.md](java/data.md) |
| Spring 与服务框架 | [java/frameworks.md](java/frameworks.md) |
| 构建、测试、观测 | [java/engineering.md](java/engineering.md) |
| 项目索引 | [java/projects.md](java/projects.md) |

## 收录标准

- 官方实现优先：OpenJDK、Spring、Linux、QEMU、Zephyr 这类仓库可以直接对机制。
- 教学系统单独标注：xv6、Nand2Tetris、OSTEP 作业用来建立心智模型，它们不是生产内核。
- 社区笔记单独标注：JavaGuide、CS-Notes 这类仓库适合检索，结论要回到官方仓库核对。
- 每条链接都指向仓库首页。大仓库在表格里写明建议先打开的目录。

## 许可

本仓库的整理文字使用 MIT 许可，见 [LICENSE](LICENSE)。链接出去的项目使用它们自己的许可证。
