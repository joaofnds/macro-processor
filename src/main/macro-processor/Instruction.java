import java.util.ArrayList;

class Instruction {
    OpCode op;
    ArrayList<Short> args = new ArrayList<>();
    OpCode.Type type;

    @Override
    public String toString() {
        return String.format("%s_%s(%s)", op, type, args.get(0));
    }

    public String toBinaryString() {
        return Integer.toString(((short) op.opcode << 8) + args.get(0), 2);
    }

}
