# Java 工程化

工程化要保证另一台机器用同一条命令得到同一类结果。这个仓库的演示就是一条这样的命令：`bash demos/run.sh`。它固定了编码、类路径和单独编译的加载器示例。

## 这条命令实际做了什么

`demos/run.sh` 的顺序是：

1. 删掉上一次的 `out`、`extra` 和 `transcripts`，避免旧 class 混进来。
2. `javac -encoding UTF-8 -d out src/*.java`。源码按 UTF-8 解析，class 放到 `out`，包名 `mapdemo` 对应子目录。
3. `javac -encoding UTF-8 -d extra src-extra/LoadedTwice.java`。这个类不能和主程序放在同一个类路径上，否则应用加载器会先定义它，两个自定义加载器的演示就失真了。
4. `python3 disasm.py` 读取 `BytecodeSample.class` 的 Code 属性，把 `add` 的指令写进 transcript。这一步不依赖系统里有没有 `javap`。
5. 每个演示用 `java -Dfile.encoding=UTF-8 -cp out` 运行。Java 17 的标准输出编码跟系统地区走，不写这个参数时，中文输出会按另一种编码落盘，和源码对不上。

输出被提交在 `demos/transcripts`。对象身份编号和 `volatile` 自增的实际值会随运行变化，正文把这种变化写明了。其余打印顺序由程序控制，用来核对过程。

## Maven 一次构建的阶段

Maven 用生命周期把同一件事排成阶段。默认生命周期里和日常有关的是：

1. `compile`：编译主代码。对应上面的第一次 `javac`。
2. `test-compile`：编译测试代码。测试代码能看见主代码，主代码看不见测试依赖。
3. `test`：运行测试。失败则构建失败。
4. `package`：打成 jar。可执行包还要带上依赖和启动类。
5. `install`：装进本地仓库，供同一机器上的别的模块依赖。

依赖用组、工件、版本三个坐标。传递依赖冲突时，Maven 按路径长度和声明顺序选一个版本。选中了哪个，用依赖树看，不要靠碰巧能启动。插件版本、JDK 版本和仓库地址写进项目，CI 使用同一条命令。

Gradle 用任务图描述同样的阶段，增量和缓存更细。多模块按“会一起变更、一起测试”的边界来切。

| 工具 | 地址 |
| --- | --- |
| Maven | https://github.com/apache/maven |
| Gradle | https://github.com/gradle/gradle |

## 测试要证明哪一层

| 层 | 证明 | 工具 |
| --- | --- | --- |
| 一个类的分支 | 给定输入得到给定输出 | JUnit 5，协作者用 Mockito |
| SQL、序列化、容器配置 | 真实组件的行为 | 该库的测试支持，或 Testcontainers 启动真实依赖 |
| 一小段计算的耗时 | 预热之后的迭代 | JMH |

替身用来隔离协作者，不用来证明 SQL 能在某种数据库上执行。涉及事务和锁的测试用真实数据库。时间从外面注入，测试里不睡固定的秒数。

本仓库的演示不是 JUnit 套件。它们把过程打印出来，供人逐行对照。业务项目里应把同样的输入和期望写成断言，而不是只把日志提交进仓库。

| 工具 | 地址 |
| --- | --- |
| JUnit 5 | https://github.com/junit-team/junit5 |
| Mockito | https://github.com/mockito/mockito |
| Testcontainers | https://github.com/testcontainers/testcontainers-java |
| JMH | https://github.com/openjdk/jmh |

## 日志、指标、规约

业务代码依赖 SLF4J 的 API，运行时再绑定 Logback 或 Log4j 2。同一条失败只打印一次完整栈。日志里带上请求或任务标识。密码、令牌和完整证件号不写进日志。

每个外部调用至少有次数、耗时和失败数。Micrometer 是应用里的指标门面，Prometheus 客户端按抓取模型暴露。追踪把一次请求穿过的进程串起来，追踪标识随日志和消息一起走。

Arthas 和 async-profiler 用在已经跑起来的进程上。采样结论要能回到具体方法。

阿里巴巴 P3C 把一组命名、并发和集合规则做成了扫描器：https://github.com/alibaba/p3c 。某条规则和当前 JDK 的机制冲突时，以语言和框架的现行行为为准，并在仓库里记下例外。

| 内容 | 地址 |
| --- | --- |
| SLF4J | https://github.com/qos-ch/slf4j |
| Log4j 2 | https://github.com/apache/logging-log4j2 |
| Micrometer | https://github.com/micrometer-metrics/micrometer |
| Prometheus Java | https://github.com/prometheus/client_java |
| SkyWalking | https://github.com/apache/skywalking |
| Arthas | https://github.com/alibaba/arthas |
| async-profiler | https://github.com/async-profiler/async-profiler |
