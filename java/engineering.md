# Java 工程化

工程化把「能在我的机器上跑」变成「可重复地构建、测试和观察」。依赖解析、测试边界和运行时指标，是代码进入生产之前就要定的结构。

## Maven 与 Gradle

Maven 的构建按生命周期推进：编译、测试、打包、安装。插件挂在阶段上。坐标是组、工件和版本。依赖调解在路径长度和声明顺序上选择一个版本，结果写进依赖树里看。版本冲突要显式定版，而不是靠传递依赖碰巧选中。

Gradle 用任务图描述同样的事，增量执行和缓存更细。多模块时，模块边界按发布单位来划：会单独变更、单独测试的代码放在一起。

构建要可重复。插件版本、JDK 版本和仓库地址写进项目，CI 使用同一套命令。

## 测试分层

| 层 | 证明什么 | 工具 |
| --- | --- | --- |
| 单元测试 | 一个类在给定输入下的分支 | JUnit 5，协作者用 Mockito 替身 |
| 组件测试 | 真实的序列化、SQL 或容器配置 | 对应库的测试支持，或 Testcontainers 拉起真实依赖 |
| 微基准 | 一小段计算的分配和耗时 | JMH |

替身用来隔离协作者，不用来证明 SQL 能在真实数据库执行。涉及事务、方言和锁的测试用真实数据库。Testcontainers 在测试生命周期里启动容器，结束时销毁。

断言写行为：返回值、状态变化、与协作者的交互。时间相关的逻辑注入时钟，避免睡觉等待。

## 日志

API 使用 SLF4J，具体实现在运行时绑定，例如 Logback。业务代码依赖 API，这样库不会把某一种日志实现绑死。

日志里带上请求或任务标识，级别按「需要人来处理」和「事后可以追查」分开。异常打印一次完整栈，避免每层都包一层再打印。密码、令牌和完整证件号不写进日志。

## 指标、追踪、在线诊断

Micrometer 把计数、定时和仪表报到监控系统。Prometheus 客户端适合按抓取模型暴露指标。每个外部调用至少有次数、耗时和失败数，否则超时只能靠猜。

SkyWalking 这类追踪把一次请求穿过的进程串起来。追踪标识要随日志和消息一起传递。

Arthas 和 async-profiler 用在已经运行的进程上：前者看方法和调用栈，后者看 CPU 与分配热点。它们是诊断工具，诊断结论要能回到代码路径。

## 静态约定

阿里巴巴 Java 编码规约和它的 P3C 检查插件，把命名、并发和集合的常见错误做成了可扫描的规则。规则适合作为团队基线。某条规则和当前 JDK 能力冲突时，以语言和框架的现行机制为准，并在仓库里记下例外。

## 项目

| 项目 | 用途 | 地址 |
| --- | --- | --- |
| Maven | 生命周期与依赖 | https://github.com/apache/maven |
| Gradle | 任务图与增量构建 | https://github.com/gradle/gradle |
| JUnit 5 | 测试运行器与断言 | https://github.com/junit-team/junit5 |
| Mockito | 测试替身 | https://github.com/mockito/mockito |
| Testcontainers Java | 用真实依赖做测试 | https://github.com/testcontainers/testcontainers-java |
| SLF4J | 日志 API | https://github.com/qos-ch/slf4j |
| Log4j 2 | 一种日志实现 | https://github.com/apache/logging-log4j2 |
| Micrometer | 应用指标门面 | https://github.com/micrometer-metrics/micrometer |
| Prometheus Java 客户端 | 按抓取模型暴露指标 | https://github.com/prometheus/client_java |
| SkyWalking | 分布式追踪 | https://github.com/apache/skywalking |
| P3C | 编码规约的检查实现 | https://github.com/alibaba/p3c |
| Arthas | 在线诊断 | https://github.com/alibaba/arthas |
| async-profiler | 采样分析 | https://github.com/async-profiler/async-profiler |
| JMH | 微基准 | https://github.com/openjdk/jmh |

阅读目标：能在一台新机器上只靠仓库里的构建文件跑起测试，并能指出一次线上变慢时先看哪个指标。
