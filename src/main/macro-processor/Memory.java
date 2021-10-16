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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

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
    private static final short MEM_SIZE = 2 << 12;
    private static final String AX = "ax";
    private static final String DX = "dx";
    private final short[] data = new short[MEM_SIZE];
    private final short si = 0;
    private BitSet sr = new BitSet(16);
    private short sp = MEM_SIZE - 1;
    private short ip = 0;
    private short dx = 0;
    private short ax = 0;

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

    private short operand(String register) {
        if (DX.equals(register)) {
            return dx;
        }
        return ax;
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
        add(DX);
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
        sub(DX);
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
        sr.set(Flags.ZERO.getValue(), result == 0);
    }

    public void cmp(short address) {
        dx = get(address);
        cmp(DX);
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
        or(DX);
    }

    public void xor(String reg) {
        var opd = operand(reg);

        ax = (short) (ax ^ opd);
    }

    public void xor(short value) {
        dx = value;
        xor(DX);
    }

    public void and(String reg) {
        var opd = operand(reg);
        ax = (short) (ax & opd);
    }

    public void and(short address) {
        dx = get(address);
        and(DX);
    }

    public void jmp(short address) {
        ip = address;
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
            case AX:
                ax = stackPop();
                break;
            case DX:
                dx = stackPop();
                break;
        }
    }

    public void popf() {
        sr = BitsetUtils.fromShort(stackPop());
    }

    public void push(String reg) {
        switch (reg) {
            case AX -> stackPush(ax);
            case DX -> stackPush(dx);
        }
    }

    public void pushf() {
        stackPush((short) BitsetUtils.toInt(sr));
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
