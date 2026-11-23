# 演示

这里的程序用来把正文里的过程跑出来。每个 `demos/src` 里的类都有一次已经跑过的输出，放在 `demos/transcripts`。正文按那次输出逐行解释。再跑一遍时，对象身份编号和 `volatile` 自增的实际值会变，其余打印顺序是固定的。

需要 JDK 17。在仓库根目录执行：

```bash
bash demos/run.sh
```

脚本会编译 `demos/src`，把 `LoadedTwice` 单独编译到 `demos/extra`（这样它不会被应用类加载器先加载），再用 UTF-8 运行每个 `main`。`add` 方法的字节码由 `demos/disasm.py` 从 class 文件里读出来，不依赖本机有没有 `javap`。

| 过程 | 源码 | 输出 |
| --- | --- | --- |
| 传参时复制的是值 | [PassByValue.java](src/PassByValue.java) | [PassByValue.txt](transcripts/PassByValue.txt) |
| 父类构造器调用可覆盖方法 | [InitOrder.java](src/InitOrder.java) | [InitOrder.txt](transcripts/InitOrder.txt) |
| 静态初始化只发生一次 | [ClassInitOnce.java](src/ClassInitOnce.java) | [ClassInitOnce.txt](transcripts/ClassInitOnce.txt) |
| 泛型擦除 | [GenericsErasure.java](src/GenericsErasure.java) | [GenericsErasure.txt](transcripts/GenericsErasure.txt) |
| 改了哈希键 | [EqualsHashCode.java](src/EqualsHashCode.java) | [EqualsHashCode.txt](transcripts/EqualsHashCode.txt) |
| 资源按声明的反序关闭 | [TryWithResources.java](src/TryWithResources.java) | [TryWithResources.txt](transcripts/TryWithResources.txt) |
| `add` 的字节码 | [BytecodeSample.java](src/BytecodeSample.java) | [BytecodeSample.javap.txt](transcripts/BytecodeSample.javap.txt) |
| 自增丢失 | [LostUpdate.java](src/LostUpdate.java) | [LostUpdate.txt](transcripts/LostUpdate.txt) |
| 线程池拒绝任务 | [ThreadPoolReject.java](src/ThreadPoolReject.java) | [ThreadPoolReject.txt](transcripts/ThreadPoolReject.txt) |
| Buffer 三个下标 | [BufferIndices.java](src/BufferIndices.java) | [BufferIndices.txt](transcripts/BufferIndices.txt) |
| 长度前缀半包 | [FrameDecoder.java](src/FrameDecoder.java) | [FrameDecoder.txt](transcripts/FrameDecoder.txt) |
| 自调用绕过代理 | [ProxySelfInvocation.java](src/ProxySelfInvocation.java) | [ProxySelfInvocation.txt](transcripts/ProxySelfInvocation.txt) |
| 循环依赖的早期缓存 | [MiniContainer.java](src/MiniContainer.java) | [MiniContainer.txt](transcripts/MiniContainer.txt) |
| 两个类加载器 | [TwoClassLoaders.java](src/TwoClassLoaders.java) | [TwoClassLoaders.txt](transcripts/TwoClassLoaders.txt) |
| 页表翻译 | [PageTableWalk.java](src/PageTableWalk.java) | [PageTableWalk.txt](transcripts/PageTableWalk.txt) |
| 缓存行 | [FalseSharing.java](src/FalseSharing.java) | [FalseSharing.txt](transcripts/FalseSharing.txt) |
| MESI | [MesiSim.java](src/MesiSim.java) | [MesiSim.txt](transcripts/MesiSim.txt) |
| TCP 握手和确认号 | [TcpHandshake.java](src/TcpHandshake.java) | [TcpHandshake.txt](transcripts/TcpHandshake.txt) |
| 启动链 | [BootChain.java](src/BootChain.java) | [BootChain.txt](transcripts/BootChain.txt) |
| 隔离级别 | [IsolationSim.java](src/IsolationSim.java) | [IsolationSim.txt](transcripts/IsolationSim.txt) |
| write 和 fsync | [WritePath.java](src/WritePath.java) | [WritePath.txt](transcripts/WritePath.txt) |
| 一次请求的十二步 | [RequestPath.java](src/RequestPath.java) | [RequestPath.txt](transcripts/RequestPath.txt) |
