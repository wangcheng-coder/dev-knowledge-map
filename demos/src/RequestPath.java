package mapdemo;

public class RequestPath {
    public static void main(String[] args) {
        String[] steps = {
                "网卡把 1500 字节以太网帧 DMA 到内存环，再发中断。CPU 不必逐字节从网卡寄存器里读。",
                "内核中断处理只记下有数据，随后协议栈取出 IP 包和 TCP 段。序号连续，数据进入套接字接收缓冲。",
                "epoll 把这个连接标记为可读，Netty 事件循环从 select 返回。",
                "事件循环把字节读进直接缓冲区。半包先留下，凑满一帧再交给解码器。",
                "HTTP 解码得到 POST /orders。请求进入 Servlet 容器的工作线程，或继续留在事件循环之外的业务池。",
                "Spring 的 Dispatcher 找到控制器方法。参数解析器把 JSON 变成 Order 对象。",
                "事务代理先从连接池取一条 JDBC 连接，执行 BEGIN。",
                "业务方法调用代理上的 create。代理里的 this.save 不经过代理，所以不会再开一层事务。",
                "JDBC 发送带占位符的 INSERT。数据库在自己的缓冲池改页，并先写预写日志。",
                "方法正常返回，代理执行 COMMIT。日志落盘后，其他已提交读的事务才能看见新行。",
                "控制器把结果交给 Jackson，工作线程写回套接字发送缓冲。",
                "内核再经 DMA 把响应帧交给网卡。"
        };
        for (int i = 0; i < steps.length; i++) {
            System.out.println((i + 1) + ". " + steps[i]);
        }
    }
}
