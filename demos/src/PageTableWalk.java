package mapdemo;

public class PageTableWalk {
    static final int PAGE = 4096;

    static final class Frame {
        final int pfn;
        final boolean present;
        Frame(int pfn, boolean present) {
            this.pfn = pfn;
            this.present = present;
        }
    }

    public static void main(String[] args) {
        Frame[] table = new Frame[1 << 20];
        int vpnPresent = 0x00401;
        table[vpnPresent] = new Frame(0x12AB, true);
        translate(table, 0x00401234L);
        translate(table, 0x00402000L);
        System.out.println("缺页处理后装入物理页 0x55");
        table[0x00402] = new Frame(0x55, true);
        translate(table, 0x00402010L);
    }

    static void translate(Frame[] table, long virtualAddress) {
        int vpn = (int) (virtualAddress / PAGE);
        int offset = (int) (virtualAddress % PAGE);
        Frame frame = table[vpn];
        System.out.println(String.format("虚拟地址 0x%08X", virtualAddress));
        System.out.println(String.format("  页号 vpn=0x%05X  页内偏移=0x%03X", vpn, offset));
        if (frame == null || !frame.present) {
            System.out.println("  页表项不存在，CPU 触发缺页，交给内核");
            return;
        }
        long physical = ((long) frame.pfn << 12) | offset;
        System.out.println(String.format("  物理页 pfn=0x%X  物理地址=0x%X", frame.pfn, physical));
    }
}
