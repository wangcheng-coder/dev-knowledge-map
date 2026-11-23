package mapdemo;

public class BootChain {
    public static void main(String[] args) {
        String[][] steps = {
                {"复位", "程序计数器被硬件置到复位向量。此时主存控制器还没训练，只能执行片内 ROM。"},
                {"bootrom", "用汇编设栈指针，初始化时钟和内存控制器。之后才可以访问 DRAM。"},
                {"拷贝下一阶段", "把 U-Boot 从闪存拷到内存，跳到它的入口。"},
                {"U-Boot", "读设备树，知道内存范围和外设地址，从磁盘或网络装入 Linux 内核和 initrd。"},
                {"内核早期", "内核用自己的页表接管地址空间，初始化中断控制器和调度器。"},
                {"第一个用户进程", "挂载根文件系统，执行 init。到这里才有 Java 虚拟机能被启动。"}
        };
        for (int i = 0; i < steps.length; i++) {
            System.out.println((i + 1) + ". " + steps[i][0]);
            System.out.println("   " + steps[i][1]);
        }
    }
}
