# 固件与软硬结合

复位之后、操作系统跑起来之前，以及操作系统里直接碰设备的那一层，都属于这里。Java 进程能打开文件、发出网络包，是因为启动链已经把 CPU、内存控制器和设备准备好，驱动已经把硬件寄存器包装成内核能调用的操作。

## 启动链

上电后程序计数器指向厂商规定的复位地址。这段代码通常在掩膜 ROM 或闪存里，工作包括：

1. 用最少量的装配代码设置栈指针。
2. 初始化时钟、内存控制器。没有这一步，后面的 C 代码没有可用的主存。
3. 把下一阶段从闪存或磁盘拷到内存并跳过去。
4. 引导加载程序装内核，或按 UEFI 的方式由固件服务加载操作系统加载器。

常见的两段是：芯片内的 bootrom，然后是 U-Boot 或 UEFI（EDK II 是 UEFI 的开源实现）。Linux 在 ARM 上还依赖设备树，用一份描述硬件的数据告诉内核「有哪些 CPU、内存范围和外设」，避免把每块板子的地址写死在驱动里。

## 裸机程序的内存布局

没有操作系统时，链接脚本决定哪段放闪存、哪段放内存、栈从哪往下长。启动文件在跳进 `main` 之前清零 BSS、拷贝已初始化数据。中断向量表把每种异常指到一个处理函数。

这和 JVM 的堆、栈不是同一层，但问题同构：每块地址有主人，生命周期要在布局时定下来。

## 驱动在做什么

驱动是硬件寄存器和内核子系统之间的翻译。一块网卡驱动要完成探测设备、申请中断和 DMA 缓冲区、把收到的帧交给协议栈、把协议栈给出的帧交给设备。

Linux 把设备分成字符设备、块设备和网络设备。字符设备按字节流读写，块设备按块访问并走页缓存，网络设备走套接字和协议栈。驱动不实现业务，它实现「如何按设备手册操作这块硬件」。

## 实时操作系统

RTOS 用在要明确响应时间的设备上。它提供任务、优先级调度、队列、信号量和软件定时器。中断服务例程仍然要短，通过队列把工作交给任务。

和 Linux 相比，RTOS 的地址空间和进程隔离往往更弱，换来的是可预期的切换开销。Zephyr 和 FreeRTOS 是两条常用的开源实现。CMSIS 是 ARM Cortex-M 上内核与外设的标准接口，很多 RTOS 和驱动包在它上面。

## 安全启动与可信执行

启动的每一段都可以校验下一段的签名，这是安全启动。ARM TrustZone 把执行分成安全世界和普通世界。OP-TEE 是跑在安全世界里的小型操作系统，普通世界的 Linux 通过规定的调用进入它。Java 应用感知不到这层，密钥和安全校验常常放在这里。

## 项目

| 项目 | 角色 | 先看什么 | 地址 |
| --- | --- | --- | --- |
| EDK II | UEFI 固件的参考实现 | `MdeModulePkg` 的启动与启动服务 | https://github.com/tianocore/edk2 |
| U-Boot | 嵌入式和开发板常用的引导程序 | `common/` 与板级目录 | https://github.com/u-boot/u-boot |
| Linux 内核模块编程指南 | 教学。字符设备、中断是怎么注册的 | 指南章节顺序 | https://github.com/sysprog21/lkmpg |
| Zephyr | 带驱动和网络栈的 RTOS | `kernel/`、`drivers/` | https://github.com/zephyrproject-rtos/zephyr |
| FreeRTOS | 任务和队列模型很直观的 RTOS 内核 | `tasks.c`、`queue.c` | https://github.com/FreeRTOS/FreeRTOS |
| CMSIS 6 | ARM Cortex 的内核与外设接口 | `CMSIS/Core` | https://github.com/ARM-software/CMSIS_6 |
| littlefs | 掉电安全的嵌入式文件系统 | `DESIGN.md` 与 `lfs.c` | https://github.com/littlefs-project/littlefs |
| OP-TEE | 可信执行环境 | `documentation/` 与 `core/` | https://github.com/OP-TEE/optee_os |

读完这一层，要能画出自己机器从上电到内核第一条 C 函数的阶段，并说清驱动、中断和 DMA 各自的职责。
