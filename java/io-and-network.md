# Java I/O 与网络

网络上的字节没有消息边界。这一页先用 `ByteBuffer` 的三个下标把“读模式”和“写模式”走完，再用一个长度前缀解码器把半包拼起来。TCP 确认号的数字过程在 [网络](../network/README.md)。

## Buffer 的 position、limit、capacity

源码 [BufferIndices.java](../demos/src/BufferIndices.java)，输出 [BufferIndices.txt](../demos/transcripts/BufferIndices.txt)。

`ByteBuffer.allocate(8)` 得到一块 8 字节的堆内缓冲。三个下标的含义是：

- `capacity` 是这块内存的长度，分配之后不变。
- `position` 是下一次读或写下标。
- `limit` 是不能越过的位置。`remaining` 等于 `limit - position`。

写入 `a`、`b`、`c` 时缓冲处于写模式，`limit` 等于 `capacity`。每 `put` 一个字节，`position` 加 1。

```text
分配 position=0 limit=8 capacity=8 remaining=8
写入 abc position=3 limit=8 capacity=8 remaining=5
flip 后准备读 position=0 limit=3 capacity=8 remaining=3
读完 abc position=3 limit=3 capacity=8 remaining=0
clear 后可以重新写 position=0 limit=8 capacity=8 remaining=8
```

`flip` 做两件事：把 `limit` 设成当前 `position`（3，表示有效数据只有 3 字节），再把 `position` 设回 0。之后 `get` 只会读到 `abc`，不会把后面 5 个未写的 0 也读出来。

读完以后 `position == limit`，再 `get` 会抛 `BufferUnderflowException`。`clear` 不是把字节擦成 0，它只把 `position` 设为 0、`limit` 设回 `capacity`，让同一块内存可以重新写。旧的 `abc` 还在内存里，直到被新写入覆盖。

堆内缓冲和本地 I/O 之间通常还要再拷贝一次。`allocateDirect` 的内存在堆外，本地调用可以少这一次，分配和释放更贵。谁分配，谁在不用时释放。

## 长度前缀：三次到达才拼出两条消息

源码 [FrameDecoder.java](../demos/src/FrameDecoder.java)，输出 [FrameDecoder.txt](../demos/transcripts/FrameDecoder.txt)。

协议规定：4 字节大端长度，后面紧跟那么多个字节的正文。解码器自己保存 `pending`。每一次 `push`：

1. 把 `pending` 和这次的字节接成 `all`。
2. 若剩下的字节不足 4 个，长度还没到齐，整段留在 `pending`。
3. 读出长度后，若剩下的字节不足 `4 + 长度`，正文还没到齐，同样整段留下，不交出半条消息。
4. 够了才切出一条字符串，`cursor` 前进，继续看后面还有没有下一条。
5. `cursor` 之后的字节成为新的 `pending`。

三次调用的字节是：

1. `00 00 00 05 68 65`。长度 5 已经看见，正文只到了 `h`、`e` 两个字节。不够 5，一条都不交出，6 字节全部留下。
2. `6c 6c 6f 00 00`。接上之后是 `00 00 00 05 68 65 6c 6c 6f 00 00`。前 9 字节正好是长度 5 的 `hello`。剩下 `00 00` 留着。
3. `00 03 48 69 21`。接上 `00 00` 变成 `00 00 00 03 48 69 21`，长度 3 的 `Hi!`。缓存变 0。

```text
收到 6 字节，解出 []，还缓存 6 字节
收到 5 字节，解出 [hello]，还缓存 2 字节
收到 5 字节，解出 [Hi!]，还缓存 0 字节
```

第二条消息的长度高位字节在第二次到达，低位和正文在第三次到达。解码器不能假设一次 `read` 等于一条消息，也不能假设一次 `read` 只含一条消息。

## 事件循环里哪些事不能做

一个线程阻塞在 `read` 上就只能守一条连接。`Selector` 登记很多通道，内核在其中有通道就绪时返回。处理就绪事件的线程要快。数据库和远程调用放到别的执行器，完成后再把写操作调度回这个通道所属的线程。否则这次慢调用期间，其余连接的字节都停在缓冲区里。

Netty 把这件事分成：接受连接的事件循环、处理已建立连接的事件循环、`ByteBuf` 的读写下标、以及按顺序挂上的解码器和业务处理器。解码器要处理的就是上面这种半包。源码从 `transport`、`buffer`、`codec` 看起：https://github.com/netty/netty

`FileChannel.transferTo` 把文件页从页缓存送到套接字，减少用户态来回拷贝。磁盘和网卡仍然要搬这些字节。

## 对照

| 步骤 | 演示或仓库 |
| --- | --- |
| 三个下标 | [BufferIndices.txt](../demos/transcripts/BufferIndices.txt) |
| 半包 | [FrameDecoder.txt](../demos/transcripts/FrameDecoder.txt) |
| TCP 确认号 | [TcpHandshake.txt](../demos/transcripts/TcpHandshake.txt) |
| JDK NIO | https://github.com/openjdk/jdk 的 `java/nio` 与 `sun/nio/ch` |
| gRPC Java | https://github.com/grpc/grpc-java |
| Protobuf 的 Java 编码 | https://github.com/protocolbuffers/protobuf |
