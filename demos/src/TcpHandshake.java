package mapdemo;

public class TcpHandshake {
    static final class End {
        final String name;
        String state;
        int seq;
        End(String name, String state, int seq) {
            this.name = name;
            this.state = state;
            this.seq = seq;
        }
    }

    static void send(End from, End to, String flags, int seq, Integer ack) {
        String ackText = ack == null ? "" : " ack=" + ack;
        System.out.println(from.name + " -> " + to.name + "  " + flags + " seq=" + seq + ackText);
    }

    public static void main(String[] args) {
        End client = new End("客户端", "CLOSED", 1000);
        End server = new End("服务端", "LISTEN", 5000);
        System.out.println("客户端 " + client.state + "，服务端 " + server.state);
        client.state = "SYN_SENT";
        send(client, server, "SYN", client.seq, null);
        server.state = "SYN_RECEIVED";
        send(server, client, "SYN+ACK", server.seq, client.seq + 1);
        client.state = "ESTABLISHED";
        send(client, server, "ACK", client.seq + 1, server.seq + 1);
        server.state = "ESTABLISHED";
        System.out.println("双方都是 ESTABLISHED。客户端下一字节序号是 1001，服务端是 5001");
        send(client, server, "PSH 数据 abc 共 3 字节", 1001, null);
        send(server, client, "ACK", 5001, 1004);
        System.out.println("服务端确认号 1004 = 1001 + 3，表示 1001、1002、1003 都收到了");
    }
}
