# Java

Java 部分按程序真正执行的顺序写。每一节能在 `demos` 里跑通，并有一份固定下来的输出。

| 顺序 | 页 | 跑这些演示 |
| --- | --- | --- |
| 1 | [语言与类型](language.md) | PassByValue、InitOrder、ClassInitOnce、GenericsErasure、EqualsHashCode、TryWithResources |
| 2 | [JVM](jvm.md) | BytecodeSample、TwoClassLoaders、ClassInitOnce |
| 3 | [并发](concurrency.md) | LostUpdate、ThreadPoolReject |
| 4 | [I/O 与网络](io-and-network.md) | BufferIndices、FrameDecoder，并对照 TCP 握手 |
| 5 | [数据访问](data.md) | IsolationSim、WritePath |
| 6 | [框架](frameworks.md) | ProxySelfInvocation、MiniContainer |
| 7 | [工程化](engineering.md) | 构建和测试的步骤，配合上面任一演示 |
| 8 | [项目索引](projects.md) | 按层跳到外部仓库 |

排障时先对现象：

| 现象 | 打开 |
| --- | --- |
| 参数改了但调用方没变，或对象字段被改了 | [语言与类型](language.md) 的传参 |
| 构造器里读到 null | [语言与类型](language.md) 的初始化 |
| 类找不到、同一份 class 变成两种类型 | [JVM](jvm.md) 的类加载 |
| 计数偏小、任务被拒绝、线程池堆内存 | [并发](concurrency.md) |
| 粘包、半包、Buffer 下标越界 | [I/O 与网络](io-and-network.md) |
| 读到未提交数据、write 返回后掉电丢失 | [数据访问](data.md) |
| 事务注解没有生效、循环依赖 | [框架](frameworks.md) |

运行全部演示：

```bash
bash demos/run.sh
```
