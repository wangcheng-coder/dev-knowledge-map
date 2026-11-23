
# Java 语言与类型

这一页按六次演示的打印顺序，把传参、构造、类初始化、泛型、哈希键和资源关闭拆开。源码在 `demos/src`，本次提交里的输出在 `demos/transcripts`。

## 传参：复制的是槽里的值

源码 [PassByValue.java](../demos/src/PassByValue.java)，输出 [PassByValue.txt](../demos/transcripts/PassByValue.txt)。

调用方有一个局部变量 `n`，值是 1；有一个局部变量 `bag`，里面存的是某个 `Bag` 对象的引用。执行 `change(n, bag, bag)` 时，虚拟机做的是：

1. 计算三个实参。`n` 的值是 1。`bag` 的值是引用。第三个实参再读一次同一个引用。
2. 为 `change` 压一个新栈帧。形参 `n`、`bag`、`slot` 是新帧里的三个槽，把上一步的三个值拷进去。
3. 方法里写 `n = 100`，改的是新帧的槽。调用方的槽还是 1。
4. 方法里写 `bag.n = 100`。这里先用拷进来的引用找到堆上的对象，再改对象里的字段。调用方的 `bag` 槽没变，但它指向的对象变了。
5. 方法里写 `slot = new Bag(7)`，只把新帧里的 `slot` 槽改成新对象的引用。调用方没有名叫 `slot` 的变量，原来的 `bag` 槽也没有被写。

本次运行的输出是：

```text
调用前: n=1 bag.n=1 对象身份=798154996
方法内一开始: n=1 bag.n=1 slot和bag是同一对象: true
方法内改完: n=100 bag.n=100 新slot.n=7
调用后: n=1 bag.n=100 对象身份=798154996
```

逐行看：

- 第一行和第四行的身份编号相同。编号每次运行都会变，相同才是结论：调用前后 `bag` 仍指向同一个对象。
- 第二行 `slot和bag是同一对象: true`，因为两个形参槽里是同一份引用的两份拷贝。
- 第三行方法内的 `n` 已经是 100，第四行调用方的 `n` 仍是 1。
- 第四行 `bag.n=100`，字段修改留在了堆对象上。新的 `Bag(7)` 没有从 `slot` 传回调用方。

所以方法可以改“引用指向的对象”，不能改“调用方那个变量现在指向谁”。

## 构造：父类构造器里调用可覆盖方法

源码 [InitOrder.java](../demos/src/InitOrder.java)，输出 [InitOrder.txt](../demos/transcripts/InitOrder.txt)。

`new Child()` 的实际顺序是：

1. 给对象分配内存，字段先是类型的默认值。`String name` 在赋值语句执行前就是 `null`。
2. 进入 `Child` 的构造器。它的第一条有效动作是调用 `Parent` 的构造器。
3. `Parent` 构造器第一行就调用 `greet()`。`greet` 在 `Child` 里被覆盖，所以进的是 `Child.greet`。此时第 6 步的字段赋值还没发生，`name` 仍是 `null`。
4. 父类构造器结束。
5. 回到子类，按源码顺序执行实例字段初始化和实例块。这时才打印“子类字段 name 初始化”，`name` 变成 `ready`。
6. 最后才执行子类构造器里 `super(...)` 之后的语句。这时读到的 `name` 才是 `ready`。

本次输出：

```text
1 准备 new Child()
3 父类构造器开始，马上调用可覆盖的 greet()
4 子类 greet 被父类构造器调用，此时 name=null
5 父类构造器结束
6 子类字段 name 初始化
7 子类构造器，name=ready
8 创建完成，name=ready
```

第 4 行就是故障现场：对象已经能调用子类方法，但子类字段还是默认值。把依赖字段的逻辑放进父类构造器调用的可覆盖方法，就会读到这个默认值。初始化动作放在构造完成之后，或者放在静态工厂里，字段赋值已经结束。

静态初始化不在这次 `new` 的后半段。它发生在类第一次被主动使用时，而且父类静态先于子类静态，见下一节。

## 类初始化只跑一次

源码 [ClassInitOnce.java](../demos/src/ClassInitOnce.java)，输出 [ClassInitOnce.txt](../demos/transcripts/ClassInitOnce.txt)。

第一次读取 `Child.c` 是主动使用。虚拟机在执行 `getstatic` 之前要先初始化 `Child`。初始化 `Child` 之前要先初始化它的父类。父类静态赋值打印第 2 行，子类静态赋值打印第 3 行。初始化结束，这个类的初始化锁被释放，状态变成已初始化。

第二次再读 `Child.c`，类已经初始化完，静态赋值不再执行，直接返回值 1。

```text
1 第一次读取 Child.c
2 父类静态初始化
3 子类静态初始化
   值=1
4 第二次读取 Child.c，静态初始化不会再来
   值=1
```

同一份类（同一个加载器定义出来的那个 `Class`）的静态初始化只有一次。两个加载器各定义一次，就会有两套静态状态，见 [JVM 的类加载](jvm.md)。

## 泛型在运行时擦成原来的类

源码 [GenericsErasure.java](../demos/src/GenericsErasure.java)，输出 [GenericsErasure.txt](../demos/transcripts/GenericsErasure.txt)。

`List<String>` 和 `List<Integer>` 在编译后都是 `List`。编译器在 `strings.add("java")` 这种调用上做类型检查，并在取出元素时插入转换。到了运行时，两个 `ArrayList` 的 `Class` 对象是同一个：

```text
两个 List 的运行时类相同: true
运行时类名: java.util.ArrayList
字节码里保留的父类签名: mapdemo.GenericsErasure$Box<java.lang.String>
签名参数: java.lang.String
```

前两行说明擦除是真的：不能因为泛型参数不同就得到两个运行时类，也不能写 `new T()`，因为运行时没有 `T` 这个类。

后两行说明签名可以另存一份。`StringBox` 的父类在字节码属性 `Signature` 里写成 `Box<java.lang.String>`。反射读的是这份属性，不是 `ArrayList` 里面那个已经擦掉的元素槽。数组和 `ArrayList` 的元素槽里没有这份参数。

## 哈希键的字段不能再改

源码 [EqualsHashCode.java](../demos/src/EqualsHashCode.java)，输出 [EqualsHashCode.txt](../demos/transcripts/EqualsHashCode.txt)。

`HashMap` 放入条目时：

1. 用当时的 `hashCode()` 选择桶。`Key(1)` 的哈希是 1，条目放进那个桶。
2. 查找时再用当前的 `hashCode()` 选桶，在桶里用 `equals` 比较。

演示接着把同一个对象的 `id` 从 1 改成 2。哈希从 1 变成 2。查找走 2 号桶，原条目还在 1 号桶，所以即使用的是原来那个对象，`get` 也返回 `null`。新的 `Key(1)` 去 1 号桶，和里面的键做 `equals`，里面的键现在 `id` 已经是 2，比较失败。新的 `Key(2)` 去 2 号桶，桶是空的。`size` 仍是 1，条目没丢，只是按新哈希找不到。

```text
放入后 get(原对象)=订单 桶位hash=1
改字段后 get(原对象)=null 新hash=2
用新的 Key(1) 查找=null
用新的 Key(2) 查找=null
map.size 仍是 1，条目还在旧桶里
```

`equals` 为真的两个对象必须有相同的 `hashCode`。参与这两个方法的字段，在对象作为键的期间不能改。

## try-with-resources 的关闭顺序

源码 [TryWithResources.java](../demos/src/TryWithResources.java)，输出 [TryWithResources.txt](../demos/transcripts/TryWithResources.txt)。

`try (Res a = new Res("A"); Res b = new Res("B"))` 展开后的顺序是：

1. 执行 `new Res("A")`，成功才有资源要关。打印“打开 A”。
2. 执行 `new Res("B")`。打印“打开 B”。若这一步失败，只关闭已经打开的 A。
3. 执行块体。`b.work()` 抛出“业务在 B 上失败”。这个异常先被记住。
4. 离开块时按声明的反序关闭：先 B，后 A。打印“关闭 B”，再打印“关闭 A”。
5. A 的 `close` 也抛异常。块体里的异常仍然是主异常，关闭异常挂到它的 suppressed 列表，避免关闭异常把原始失败盖住。

```text
打开 A
打开 B
关闭 B
关闭 A
主异常: 业务在 B 上失败
被压制的关闭异常: 关闭 A 失败
```

连接、流、锁都按这个结构交还：在头部声明，在块里使用，不在 `finalize` 里碰运气。

## 标准库入口

| 要核对的合同 | 先看 | 地址 |
| --- | --- | --- |
| `Object`、`String`、类初始化相关的语言库 | `src/java.base/share/classes/java/lang` | https://github.com/openjdk/jdk |
| `HashMap` 的桶和 `equals` | `src/java.base/share/classes/java/util/HashMap.java` | https://github.com/openjdk/jdk |
| `javac` 如何生成字节码 | `src/jdk.compiler` | https://github.com/openjdk/jdk |
