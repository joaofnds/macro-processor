/*
~      Unary bitwise complement
<<     Signed left shift
>>     Signed/Arithmetic right shift
>>>    Unsigned/Logical right shift
&      Bitwise AND
^      Bitwise exclusive OR
|      Bitwise inclusive OR
*/

import java.io.IOException;
import java.util.BitSet;
import java.util.Stack;

public class Memory {
    private static final short MEM_SIZE = 2 << 12;
    private final short[] data = new short[MEM_SIZE];
    private final short si = 0;
    private final short highMask = (short) 0xff00;
    private final short lowMask = (short) 0x00ff;
    private short sp = MEM_SIZE;
    private boolean cf = false;
    private boolean pf = false;
    private boolean inf = false;
    private boolean sf = false;
    private boolean of = false;
    private short ip = 0;
    private boolean zf = false;
    private short dx = 0;
    private short ax = 0;

    public Memory() {
    }

    public static void main(String[] args) {
        var mem = new Memory();
    }

    private short operand(String register) {
        switch (register) {
            case "DX":
                return dx;
            default:
                return ax;
        }
    }

    public void storeAx(short address) {
        assertValidAddress(address);

        data[address] = ax;
    }

    public void storeDx(short address) {
        assertValidAddress(address);

        data[address] = dx;
    }

    public void accByPass(short data) { // TODO: remove
        ax = data;
    }

    public short get(short address) {
        assertValidAddress(address);

        return data[address];
    }

    public void add(String reg) {
        var opd = operand(reg);

        ax = (short) (ax + opd);
    }

    public void add(short address) {
        dx = get(address);
        add("DX");
    }

    public void divSi() {
        ax = (short) (ax / si);
        dx = (short) (ax % si);
    }

    public void divAx() {
        ax = 1;
        dx = 0;
    }

    public void sub(String reg) {
        var opd = operand(reg);
        ax = (short) (ax - opd);
    }

    public void sub(short address) {
        dx = get(address);
        sub("DX");
    }

    public void mul() {
        ax = (short) (ax * ax);
        dx = ax;
    }

    public void mulSi() {
        ax = (short) (ax * si);
        dx = ax;
    }

    public void cmp(String reg) {
        var result = operand(reg);
        zf = result == 0;
    }

    public void cmp(short address) {
        dx = get(address);
        cmp("DX");
    }

    public void not() {
        ax = (short) ~ax;
    }

    public void or(String reg) {
        var opd = operand(reg);
        ax = (short) (ax | opd);
    }

    public void or(short address) {
        dx = this.get(address);
        or("DX");
    }

    public void xor(String reg) {
        var opd = operand(reg);

        ax = (short) (ax ^ opd);
    }

    public void xor(short value) {
        dx = value;
        xor("DX");
    }

    public void and(String reg) {
        var opd = operand(reg);
        ax = (short) (ax & opd);
    }

    public void and(short address) {
        dx = get(address);
        and("DX");
    }

    public void jmp(short address) {
        ip = address;
    }

    public void jz(short address) {
        if (zf) {
            ip = address;
        }
    }

    public void jnz(short address) {
        if (!zf) {
            ip = address;
        }
    }

    public void jp(short address) {
        if (!sf) {
            ip = address;
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

    public void pop(String reg) {
        switch (reg) {
            case "AX":
                ax = stackPop();
                break;
            case "DX":
                dx = stackPop();
                break;
        }
    }

    public void popf() {
        var value = stackPop();

        cf = getBit(value, 0);
        pf = getBit(value, 6);
        inf = getBit(value, 7);
        zf = getBit(value, 8);
        sf = getBit(value, 9);
        of = getBit(value, 12);
    }

    public void push(String reg) {
        switch (reg) {
            case "AX":
                stackPush(ax);
                break;
            case "DX":
                stackPush(dx);
                break;
        }
    }

    public void pushf() {
        var sr = new BitSet(16);

        sr.set(12, of);
        sr.set(9, sf);
        sr.set(8, zf);
        sr.set(7, inf);
        sr.set(6, pf);
        sr.set(0, cf);

        stackPush(bitSetToShort(sr));
    }

    private void write(short address) {
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
        var stackValue = data[sp];
        return stackValue;
    }

    private void stackPush(short opd) {
        data[sp] = opd;
        sp -= 1;
    }

    private boolean getBit(int n, int k) {
        var bitValue = (n >> k) & 1;
        return bitValue == 1;
    }

    private short bitSetToShort(BitSet bs) {
        var result = (short) 0;

        for (int i = 0; i < bs.length(); i++) {
            var bitValue = (short) (bs.get(i) ? 1 : 0);
            result = (short) (result ^ bitValue);
            result = (short) (result << 1);
        }

        return result;
    }

    private void assertValidAddress(short address) {
        if (address > MEM_SIZE) {
            throw new Error("address out of bounds");
        }
    }
}
