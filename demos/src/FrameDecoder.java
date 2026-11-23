package mapdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrameDecoder {
    static final class Decoder {
        byte[] pending = new byte[0];

        List<String> push(byte[] chunk) {
            byte[] all = new byte[pending.length + chunk.length];
            System.arraycopy(pending, 0, all, 0, pending.length);
            System.arraycopy(chunk, 0, all, pending.length, chunk.length);
            int cursor = 0;
            List<String> messages = new ArrayList<>();
            while (all.length - cursor >= 4) {
                int length = ((all[cursor] & 0xff) << 24)
                        | ((all[cursor + 1] & 0xff) << 16)
                        | ((all[cursor + 2] & 0xff) << 8)
                        | (all[cursor + 3] & 0xff);
                if (all.length - cursor < 4 + length) {
                    break;
                }
                cursor += 4;
                messages.add(new String(all, cursor, length));
                cursor += length;
            }
            pending = Arrays.copyOfRange(all, cursor, all.length);
            System.out.println("收到 " + chunk.length + " 字节，解出 " + messages + "，还缓存 " + pending.length + " 字节");
            return messages;
        }
    }

    public static void main(String[] args) {
        Decoder decoder = new Decoder();
        decoder.push(new byte[] {0, 0, 0, 5, 'h', 'e'});
        decoder.push(new byte[] {'l', 'l', 'o', 0, 0});
        decoder.push(new byte[] {0, 3, 'H', 'i', '!'});
    }
}
