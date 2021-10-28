import java.util.ArrayList;

class Instruction {
    OpCode op;
    ArrayList<Short> args = new ArrayList<>();
    OpCode.Type type;

    @Override
    public String toString() {
        return String.format("%s_%s(%s)", op.mnemonic, type, args.get(0));
    }
}
