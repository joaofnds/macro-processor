/*
~      Unary bitwise complement
<<     Signed left shift
>>     Signed/Arithmetic right shift
>>>    Unsigned/Logical right shift
&      Bitwise AND
^      Bitwise exclusive OR
|      Bitwise inclusive OR
*/

import utils.BitsetUtils;

import java.io.IOException;
import java.util.BitSet;

enum Flags {
    CARRY(0),
    PARITY(6),
    INTERRUPT(7),
    ZERO(8),
    SIGN(9),
    OVERFLOW(12);

    private final int value;

    Flags(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class Memory {
    static final short AX = 0b11110000;
    static final short DX = 0b11110000;

    private static final short MEM_SIZE = 2 << 12;
    public final short[] data = new short[MEM_SIZE];
    public final short opStart = 0;
    private final short symbolOffset = 2 << 10;
    private final short si = 0;
    public short ip = opStart;
    private BitSet sr = new BitSet(16);
    private short sp = MEM_SIZE - 1;
    private short dx = 0;
    private short ax = 0;
    private short symbolPointer = symbolOffset;

    public Memory() {
    }

    public static void main(String[] args) {
        var bs = new BitSet(16);
        var result = (short) 5;

        for (int i = 0; i < bs.size(); i++) {
            bs.set(i, ((result >> i) & 1) == 1);
        }

        System.out.println(bs);
    }

    public void storeAx(short address) {
        assertValidAddress(address);

        data[address] = ax;
    }

    public void storeDx(short address) {
        assertValidAddress(address);

        data[address] = dx;
    }

    public short get(short address) {
        assertValidAddress(address);

        return data[address];
    }

    public short readOp() {
        return data[ip++];
    }

    public void storeOp(short op, short arg) {
        data[ip++] = op;
        data[ip++] = arg;
    }

    public void storeSymbol(short value) {
        data[symbolPointer] = value;
        symbolPointer++;
    }

    public short symbolAddress(short offset) {
        return (short) (symbolOffset + offset);
    }

    public void add(short address) {
        ax += get(address);
    }

    public void divSi() {
        ax = (short) (ax / si);
        dx = (short) (ax % si);
    }

    public void divAx() {
        ax = 1;
        dx = 0;
    }

    public void sub(short address) {
        ax -= get(address);
    }

    public void mul() {
        ax = (short) (ax * ax);
        dx = ax;
    }

    public void mulSi() {
        ax = (short) (ax * si);
        dx = ax;
    }

    public void cmp(short address) {
        dx = get(address);
        sr.set(Flags.ZERO.getValue(), ax == dx);
        sr.set(Flags.SIGN.getValue(), ax < dx);
    }

    public void not() {
        ax = (short) ~ax;
    }

    public void or(short address) {
        dx = this.get(address);
        or(DX);
    }

    public void xor(short value) {
        dx = value;
        xor(DX);
    }

    public void and(short address) {
        dx = get(address);
        and(DX);
    }

    public void jmp(short address) {
        // each instruction is followed by its args, so we need to
        // double the offset to point to the correct instruction
        short offset = data[address];
        ip = (short) (offset * 2);
    }

    public void jz(short address) {
        if (sr.get(Flags.ZERO.getValue())) {
            ip = address;
        }
    }

    public void jnz(short address) {
        if (!sr.get(Flags.ZERO.getValue())) {
            ip = address;
        }
    }

    public void jp(short address) {
        if (!sr.get(Flags.SIGN.getValue())) {
            jmp(address);
        }
    }

    public void call(short address) {
        stackPush(ip);
        jmp(address);
    }

    public void ret() {
        ip = stackPop();
    }

    public void hlt() {
        System.exit(0);
    }

    public void pop(short address) {
        data[address] = stackPop();
    }

    public void popf() {
        sr = BitsetUtils.fromShort(stackPop());
    }

    public void pushf() {
        stackPush((short) BitsetUtils.toInt(sr));
    }

    void write(short address) {
        System.out.println(get(address));
    }

    private void read(short address) throws IOException {
        var in = System.in.read();
        data[address] = (short) in;
    }

    private short stackPop() {
        if (sp == MEM_SIZE) {
            throw new Error("empty stack");
        }

        sp += 1;
        return data[sp];
    }

    private void stackPush(short opd) {
        data[sp] = opd;
        sp -= 1;
    }

    private void assertValidAddress(short address) {
        if (address > MEM_SIZE) {
            throw new Error("address out of bounds");
        }
    }

}
