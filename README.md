# 开发知识地图

这个仓库按一次真实调用的执行顺序，把硬件、固件、操作系统、网络、运行时和 Java 讲开。每一节都对应 `demos` 里的一个程序和一份已经跑出来的输出。先看输出里的每一行是什么，再回到正文看这一行之前机器做了哪几步。

总的那一次调用在 [demos/src/RequestPath.java](demos/src/RequestPath.java)。它按顺序打印从网卡 DMA 到 JDBC 提交的 12 步，输出在 [demos/transcripts/RequestPath.txt](demos/transcripts/RequestPath.txt)。

## 怎么读

1. 运行 `bash demos/run.sh`，或直接打开 `demos/transcripts`。
2. 从下面的层进入。每一层先走演示，再给出这一层在生产系统里的源码入口。
3. 日常写 Java 时从 [java/README.md](java/README.md) 进入。一个现象对不上时，回到产生这个现象的那一层，而不是先改业务判断。

| 层 | 这一层的过程写在哪 | 先跑哪个演示 |
| --- | --- | --- |
| 硬件 | [hardware](hardware/README.md) | 缓存行、MESI |
| 固件 | [firmware](firmware/README.md) | 启动链 |
| 操作系统 | [os](os/README.md) | 页表、write 与 fsync |
| 网络 | [network](network/README.md) | TCP 握手 |
| 运行时 | [runtime](runtime/README.md) | `add` 的字节码 |
| Java | [java](java/README.md) | 传参、类加载、线程池、半包、事务代理 |

演示索引和运行方法在 [demos/README.md](demos/README.md)。

## 和源码的关系

演示把机制缩成能打印的步骤。生产实现比演示长：HotSpot 的 GC、Linux 的页表、Netty 的流水线都还有大量分支。正文在每层末尾给出仓库和先打开的目录，用来顺着同一步骤读下去。

本仓库的整理文字使用 MIT 许可，见 [LICENSE](LICENSE)。链接出去的项目使用它们自己的许可证。
