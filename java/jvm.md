# JVM

这一页用三次能核对的材料讲执行：`add` 的真实字节码、类初始化一次、两个加载器定义出两个类。后面的对象布局、GC 和 JIT 按 HotSpot 的实际阶段写，每一阶段都能在 OpenJDK 的 `src/hotspot` 里找到对应目录。

## `add` 的每一条字节码

源码 [BytecodeSample.java](../demos/src/BytecodeSample.java)。`demos/disasm.py` 从编译后的 class 文件读出 `add` 的 Code 属性，输出在 [BytecodeSample.javap.txt](../demos/transcripts/BytecodeSample.javap.txt)：

```text
方法 add(II)I
max_stack=2 max_locals=3 code_length=6
   0: iload_0
   1: iload_1
   2: iadd
   3: istore_2
   4: iload_2
   5: ireturn
```

`add` 是静态方法，描述符 `(II)I`。局部变量表没有 `this`：0 号槽是 `left`，1 号槽是 `right`，2 号槽是 `sum`。所以 `max_locals=3`。

调用 `add(20, 22)` 时：

1. 调用方把 20 和 22 压进操作数栈，执行 `invokestatic`。
2. 新栈帧建立，20 落在 0 号槽，22 落在 1 号槽，操作数栈是空的。
3. `iload_0` 把 20 压栈。栈：`[20]`。
4. `iload_1` 把 22 压栈。栈：`[20, 22]`。这一步之后栈深是 2，所以 `max_stack=2`。
5. `iadd` 弹出两个 int，压入 42。栈：`[42]`。
6. `istore_2` 把 42 弹出，写入 2 号槽。栈空。源码里的局部变量 `sum` 就是这个槽。
7. `iload_2` 再把 42 压栈。
8. `ireturn` 弹出 42，丢弃这个帧，调用方的 `main` 拿到返回值。`main` 打印 42，见 [BytecodeSample.txt](../demos/transcripts/BytecodeSample.txt)。

解释器就是按程序计数器逐条做上面的事。程序计数器是线程私有的，记录的是下一条字节码在这个方法里的偏移。

## 类的身份包含加载器

源码 [TwoClassLoaders.java](../demos/src/TwoClassLoaders.java)，被加载的类在 [LoadedTwice.java](../demos/src-extra/LoadedTwice.java)。它被单独编译到 `demos/extra`，不在应用类路径上，所以应用加载器不会先把它定义掉。两个 `URLClassLoader` 的父加载器是平台加载器，它们都会自己找到这份字节并定义类。

输出 [TwoClassLoaders.txt](../demos/transcripts/TwoClassLoaders.txt)：

```text
两个加载器得到的 Class 是同一个对象: false
名字都是: LoadedTwice
第二个加载器的类型能否接纳第一个的实例: false
```

名字相同，`Class` 对象不同。第一个加载器创建的实例，用第二个加载器的 `Class.isInstance` 判断为假。之后如果把这个实例传给只认识第二种 `LoadedTwice` 的代码，转换会失败。静态字段也会有两份，因为静态状态挂在 `Class` 上。

委派的方向是先问父加载器。平台加载器的路径里没有 `LoadedTwice`，两个子加载器才各自定义。核心类走父加载器，是为了让 `java.lang.Object` 只有一份。应用服务器把 Web 应用的类放在子加载器、并且有时让子加载器先查自己的目录，是为了让两个应用各带一份同名库。SPI 则在父加载器的代码里，用线程上下文类加载器再去子加载器找实现。

类初始化和加载不是同一步。加载是把字节定义成 `Class`。初始化是执行静态赋值，而且每个 `Class` 只有一次。同一次运行里连续读 `Child.c` 的输出在 [ClassInitOnce.txt](../demos/transcripts/ClassInitOnce.txt)：父类静态、子类静态各打印一次，第二次读取不再打印。

## 创建一个普通对象时虚拟机做的事

以 `new Child()` 为例，字节码层面是 `new`、`dup`、`invokespecial <init>`。`<init>` 里的顺序已经由 [InitOrder](language.md) 跑出来了。虚拟机在 `new` 和 `<init>` 之间做这些事：

1. 在常量池里找到 `Child` 的符号引用。还没加载就先加载，还没初始化就先初始化类。
2. 按类的布局算对象大小。对象头有标记字和类型指针，后面是字段，末尾补齐到 8 字节。压缩普通对象指针打开时，类型指针和引用字段用 32 位。
3. 在当前线程的 TLAB 里划出这块内存。TLAB 用完才去堆里再要一块，避免每次分配都抢堆指针。
4. 内存清成默认值。这就是构造器跑到字段赋值之前，引用字段仍是 `null`、数值字段仍是 0 的原因。
5. 写入类型指针，让这片内存成为一个 `Child`。
6. 执行 `<init>`。父类构造器、字段赋值、子类构造器体按 [语言页](language.md) 的顺序发生。

JOL 可以打印真实布局和对齐，地址是 https://github.com/openjdk/jol 。没有 JOL 时不要用字段个数乘 8 去估计对象大小，对象头和对齐会把结果算小。

## 一次年轻代回收的步骤

分代收集认为大多数对象很快就死。一次年轻代拷贝按这个顺序发生：

1. 从根出发找活对象。根包括各线程栈和寄存器里的引用、静态字段、JNI 引用。
2. 活对象被复制到一块空的存活区，引用被改到新地址。死对象所在的区域整块丢掉，不逐个调用释放函数。
3. 熬过若干次年轻代回收的对象晋升到老年代。老年代用不同的算法整理或标记。
4. 应用线程在安全点停下来，回收器才能移动对象或扫描栈。停顿就是这段时间。

| 收集器 | 它把停顿和吞吐怎么摆 | 源码目录 |
| --- | --- | --- |
| Serial / Parallel | 年轻代拷贝。Parallel 用多线程提高吞吐 | `src/hotspot/share/gc/serial`、`gc/parallel` |
| G1 | 堆切成等大区域。混合回收时挑垃圾比例高的区域，用来把大堆的停顿拆开 | `src/hotspot/share/gc/g1` |
| ZGC | 标记和重定位尽量并发。读屏障保证对象被搬走时访问仍能进行 | `src/hotspot/share/gc/z` |

JDK 21 及以后，分代 ZGC 是低延迟方向的主线。某个发行版的默认收集器以它的发布说明为准。

看停顿时先读 GC 日志里的分配速率和晋升量。年轻代每次都满，通常是短命对象太多，而不是老年代算法选错。

## 解释执行之后的即时编译

新方法先由解释器跑，同时累计调用次数和回边次数。到达阈值后：

1. C1 较快地编出带计数的机器码，方法开始以机器码运行。
2. 方法仍然热，C2 做更重的优化：内联、逃逸分析、基于当时类型的分支优化。
3. 优化依赖的假设失效，例如内联的调用目标变了，机器码被丢掉，执行回到解释或重新编译。这叫去优化。

逃逸分析如果证明对象不会离开这个方法，可以把字段拆成寄存器里的值，堆分配在这个方法里就消失了。微基准若没有预热，量到的是解释和编译混在一起的时间。JMH 负责预热、防止死代码被删掉、以及多次迭代，地址是 https://github.com/openjdk/jmh 。

## 诊断时打开的仓库

| 要看的阶段 | 地址 |
| --- | --- |
| 解释器、编译器、GC | https://github.com/openjdk/jdk |
| 对象布局 | https://github.com/openjdk/jol |
| 微基准 | https://github.com/openjdk/jmh |
| 采样 CPU 和分配 | https://github.com/async-profiler/async-profiler |
| 看已经加载的类和方法 | https://github.com/alibaba/arthas |
