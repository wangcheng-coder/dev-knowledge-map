
import struct
import sys
from pathlib import Path

OPS = {
    0x1A: "iload_0",
    0x1B: "iload_1",
    0x1C: "iload_2",
    0x1D: "iload_3",
    0x3B: "istore_0",
    0x3C: "istore_1",
    0x3D: "istore_2",
    0x3E: "istore_3",
    0x60: "iadd",
    0xAC: "ireturn",
    0xB1: "return",
}

def u2(data, i):
    return struct.unpack_from(">H", data, i)[0], i + 2

def u4(data, i):
    return struct.unpack_from(">I", data, i)[0], i + 4

def skip_pool(data, i):
    count, i = u2(data, i)
    pool = [None]
    while len(pool) < count:
        tag = data[i]
        i += 1
        if tag == 1:
            ln, i = u2(data, i)
            pool.append(data[i:i + ln].decode("utf-8"))
            i += ln
        elif tag in (7, 8, 16, 19, 20):
            i += 2
            pool.append(tag)
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            i += 4
            pool.append(tag)
        elif tag in (5, 6):
            i += 8
            pool.append(tag)
            pool.append(None)
        elif tag == 15:
            i += 3
            pool.append(tag)
        else:
            raise SystemExit(f"unknown cp tag {tag} at {i}")
    return pool, i

def main():
    data = Path(sys.argv[1]).read_bytes()
    i = 8
    pool, i = skip_pool(data, i)
    i += 6
    ic, i = u2(data, i)
    i += ic * 2
    fc, i = u2(data, i)
    for _ in range(fc):
        i += 6
        ac, i = u2(data, i)
        for _ in range(ac):
            i += 2
            ln, i = u4(data, i)
            i += ln
    mc, i = u2(data, i)
    for _ in range(mc):
        i += 2
        name_i, i = u2(data, i)
        desc_i, i = u2(data, i)
        name = pool[name_i]
        desc = pool[desc_i]
        ac, i = u2(data, i)
        for _ in range(ac):
            an, i = u2(data, i)
            ln, i = u4(data, i)
            body = data[i:i + ln]
            i += ln
            if pool[an] == "Code" and name == "add":
                max_stack, pos = u2(body, 0)
                max_locals, pos = u2(body, pos)
                code_len, pos = u4(body, pos)
                code = body[pos:pos + code_len]
                print(f"方法 {name}{desc}")
                print(f"max_stack={max_stack} max_locals={max_locals} code_length={code_len}")
                pc = 0
                while pc < len(code):
                    op = code[pc]
                    print(f"{pc:4d}: {OPS.get(op, hex(op))}")
                    pc += 1

if __name__ == "__main__":
    main()
