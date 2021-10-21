import java.util.ArrayList;

class Instruction {
    String op;
    ArrayList<Short> args = new ArrayList<>();
    OpCode.Type type;

    @Override
    public String toString() {
        return String.format("%s_%s(%s)", op, type, args.get(0));
    }
}
