/*
~      Unary bitwise complement
<<     Signed left shift
>>     Signed/Arithmetic right shift
>>>    Unsigned/Logical right shift
&      Bitwise AND
^      Bitwise exclusive OR
|      Bitwise inclusive OR
*/

public class Memory {
    private static final short MEM_SIZE = 2 << 12;
    private final short[] data = new short[MEM_SIZE];
    private final short sp = 0;
    private final short si = 0;
    private final boolean cf = false;
    private final boolean pf = false;
    private final boolean inf = false;
    private final boolean sf = false;
    private final boolean of = false;
    private final short highMask = (short) 0xff00;
    private final short lowMask = (short) 0x00ff;
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

    private void assertValidAddress(short address) {
        if (address > MEM_SIZE) {
            throw new Error("address out of bounds");
        }
    }
}
