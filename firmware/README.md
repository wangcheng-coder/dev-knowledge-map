# 固件与软硬结合

复位之后、Java 进程出现之前，机器按固定阶段把内存和外设准备好。演示把这六段打印出来：[BootChain.txt](../demos/transcripts/BootChain.txt)，源码 [BootChain.java](../demos/src/BootChain.java)。

## 六段里每一段内存处于什么状态

```text
1. 复位
   程序计数器被硬件置到复位向量。此时主存控制器还没训练，只能执行片内 ROM。
2. bootrom
   用汇编设栈指针，初始化时钟和内存控制器。之后才可以访问 DRAM。
3. 拷贝下一阶段
   把 U-Boot 从闪存拷到内存，跳到它的入口。
4. U-Boot
   读设备树，知道内存范围和外设地址，从磁盘或网络装入 Linux 内核和 initrd。
5. 内核早期
   内核用自己的页表接管地址空间，初始化中断控制器和调度器。
6. 第一个用户进程
   挂载根文件系统，执行 init。到这里才有 Java 虚拟机能被启动。
```

逐段看能使用的存储：

1. 复位时程序计数器被硬件设到厂商规定的地址。DRAM 的训练还没做，那块内存不能用。能执行的只有片内 ROM 或片内 SRAM。
2. bootrom 先用汇编设好栈指针。没有栈，C 的调用就没有返回地址可放。然后初始化时钟和内存控制器。这一步结束，后面的代码才可以读写 DRAM。
3. 下一阶段的程序还在闪存里。把 U-Boot 拷到 DRAM 的指定地址，再跳过去。跳之前要保证目标地址的指令缓存和数据缓存不会各看各的旧内容。
4. U-Boot 读设备树。设备树用数据描述有哪些 CPU、内存从哪到哪、外设寄存器在哪，避免把每块板子的地址写死在驱动里。然后它从磁盘或网络把 Linux 内核和 initrd 装进内存。
5. 内核建立自己的页表，之后的地址是虚拟地址。它初始化中断控制器，设备才能用中断而不是让 CPU 空转查询。调度器起来之后，才有“线程”这个概念。
6. 根文件系统挂上，init 成为第一个用户进程。Java 虚拟机是这个用户进程的后裔，它看不到复位向量，也没有权限直接写设备寄存器。

## 裸机程序的布局

没有操作系统时，链接脚本决定哪一段放闪存、哪一段放内存、栈从哪往下长。启动文件在调用 `main` 之前清零 BSS、把已初始化数据从闪存拷到内存。中断向量表把每种异常指到一个函数。

这和 JVM 的堆不是同一层，但分配规则相同：每块地址有主人，主人要在布局时定下来。

## 驱动做的翻译

驱动把设备手册上的寄存器操作翻译成内核子系统能调用的函数。网卡驱动要探测设备、申请中断号、准备 DMA 环，收到帧后交给协议栈，再把协议栈给的帧写回发送环。它不实现订单业务。

Linux 把设备分成字符设备、块设备和网络设备。字符设备按字节流，块设备按块并走页缓存，网络设备走套接字。页缓存和 `write` 返回的差别在 [操作系统](../os/README.md)。

## 实时系统上的任务

RTOS 提供任务、优先级、队列和定时器。中断服务例程仍然要短，通过队列把工作交给任务。和 Linux 相比，它的地址空间隔离往往更弱，换来的是更可预期的切换时间。

Zephyr 的内核和驱动在 https://github.com/zephyrproject-rtos/zephyr 。FreeRTOS 的任务和队列在 https://github.com/FreeRTOS/FreeRTOS 的 `tasks.c`、`queue.c`。ARM Cortex-M 的接口在 https://github.com/ARM-software/CMSIS_6 。掉电安全的小文件系统 littlefs 说明了“写到一半断电”在闪存上怎么恢复：https://github.com/littlefs-project/littlefs 。

UEFI 固件的参考实现是 https://github.com/tianocore/edk2 。U-Boot 在 https://github.com/u-boot/u-boot 。Linux 字符设备和中断注册的教学在 https://github.com/sysprog21/lkmpg 。安全启动逐段校验下一段；OP-TEE 跑在 ARM 的安全世界里：https://github.com/OP-TEE/optee_os 。
