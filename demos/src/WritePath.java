package mapdemo;

public class WritePath {
    public static void main(String[] args) {
        String[][] steps = {
                {"用户缓冲区", "Java 把 4096 字节放进堆里的 byte[]。"},
                {"系统调用 write", "CPU 陷入内核，检查文件描述符和地址。"},
                {"页缓存", "内核把这 4096 字节拷进对应文件页，页被标成脏。"},
                {"write 返回", "调用方以为写完了。磁盘上的旧页还在。"},
                {"进程崩溃", "页缓存还在，数据还在。"},
                {"整机掉电", "没有 fsync 时，脏页丢失，磁盘仍是旧内容。"},
                {"fsync", "内核把脏页和文件系统日志写到盘，并等待设备确认。"}
        };
        for (int i = 0; i < steps.length; i++) {
            System.out.println((i + 1) + ". " + steps[i][0] + "：" + steps[i][1]);
        }
    }
}
