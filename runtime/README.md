# 语言运行时

运行时把文本变成 CPU 能执行的指令，并在进程活着的时候管理代码和堆。Java 的 `javac` 停在字节码，真正的机器码由虚拟机里的即时编译器产生。这一页用已经反汇编出来的 `add` 说明编译停在哪里，虚拟机从哪里接上。

## `add` 从源码到六条字节码

[BytecodeSample.java](../demos/src/BytecodeSample.java) 里的方法是：

```java
static int add(int left, int right) {
    int sum = left + right;
    return sum;
}
```

`javac` 做的是前端和到字节码的生成，不做本机寄存器分配：

1. 词法把 `int`、标识符、`+`、`return` 分成记号。
2. 语法把方法体变成一棵树：一次加法，一次局部变量赋值，一次返回。
3. 语义分析确认三个量都是 int，加法不需要转换。
4. 生成字节码。静态方法没有 `this`，所以 0 号槽是 `left`，1 号槽是 `right`，2 号槽是 `sum`。
5. 写入 class 文件的 Code 属性：`max_stack=2`，`max_locals=3`，六条指令。

[BytecodeSample.javap.txt](../demos/transcripts/BytecodeSample.javap.txt) 是从这份 class 文件解析出来的，不是手写的清单：

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

每一条在栈上的变化写在 [JVM](../java/jvm.md)。这里要分开的是责任：`javac` 保证这六条的类型是对的；HotSpot 解释器负责执行它们；热点之后 C1/C2 才把它们变成机器码。机器码使用的是硬件页里那套寄存器和调用约定，不再是 `iload_0` 这种槽号。

## 链接和进程装载

C 程序还会多两步。编译器产出的目标文件里地址没有完全定死，链接器解析符号、做重定位，动态链接把最后一步推迟到进程启动。加载器按文件头建立代码段、数据段、堆和栈，然后跳到入口。

Java 程序的入口是虚拟机本身。虚拟机再按类加载过程定义 `main` 所在的类。两个加载器定义同名类会得到两个 `Class`，演示在 [TwoClassLoaders.txt](../demos/transcripts/TwoClassLoaders.txt)。

JNI 调用本地库时必须遵守平台的调用约定：参数在哪些寄存器、栈谁来清、返回值放哪。本地代码里的崩溃不会变成 Java 异常。

## 谁负责释放内存

手动管理要求每块堆内存有一个释放点。错误是泄漏、重复释放和释放后继续用。垃圾回收要求从根出发能说清哪些对象还能到达。根包括线程栈、静态字段和 JNI 引用。到达不了的对象可以被回收。

回收的代价是停顿、读写屏障和额外内存。静态集合、没取消的监听器和类加载器都会把对象留在根上，回收器不能把它们当成垃圾。

| 通用阶段 | 在 Java 里 |
| --- | --- |
| 前端 | `javac` 写 class 文件 |
| 中间码 | 字节码 |
| 优化和后端 | HotSpot 的 C1、C2，或 Graal |
| 装载 | 类加载器 |
| 堆 | 垃圾回收器 |
| 本地边界 | JNI |

LLVM 把中间表示和多种目标做成了可复用的编译器：https://github.com/llvm/llvm-project 。GCC 是另一套完整编译器：https://github.com/gcc-mirror/gcc 。OpenJDK 同时包含 `javac` 和 HotSpot：https://github.com/openjdk/jdk 。Protobuf 则是“结构如何变成字节，再被另一种语言的运行时读出”的独立项目：https://github.com/protocolbuffers/protobuf 。
