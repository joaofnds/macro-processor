import java.util.ArrayList;

class Instruction {
    OpCode op;
    ArrayList<Short> args = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("%s_%s(%s)", op.mnemonic, op.type, args.get(0));
    }
}
