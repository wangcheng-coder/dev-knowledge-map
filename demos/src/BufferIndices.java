package mapdemo;

import java.nio.ByteBuffer;

public class BufferIndices {
    static void show(String step, ByteBuffer buf) {
        System.out.println(step + " position=" + buf.position() + " limit=" + buf.limit() + " capacity=" + buf.capacity() + " remaining=" + buf.remaining());
    }

    public static void main(String[] args) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        show("分配", buf);
        buf.put((byte) 'a').put((byte) 'b').put((byte) 'c');
        show("写入 abc", buf);
        buf.flip();
        show("flip 后准备读", buf);
        StringBuilder text = new StringBuilder();
        while (buf.hasRemaining()) {
            text.append((char) buf.get());
        }
        show("读完 " + text, buf);
        buf.clear();
        show("clear 后可以重新写", buf);
    }
}
