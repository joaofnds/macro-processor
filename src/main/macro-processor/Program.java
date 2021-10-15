import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum State {NORMAL, SEGMENT}

enum Instruction {ADD, SUB, PUSH, POP, MOV, MUL, UNKNOWN}

public class Program {
    Memory memory = new Memory();
    State state = State.NORMAL;
    String lastSegmentStart = "";

    public Program(String inputFile) throws IOException {
        var program = expandMacro(inputFile);
        clean(program).forEach(this::execute);
    }

    public static void main(String[] args) throws IOException {
        var inputFile = "src/main/resources/program.asm";
        new Program(inputFile);
    }

    private ArrayList<String> expandMacro(String inputFile) throws IOException {
        return new MacroProcessor(inputFile).processMacro();
    }

    private void execute(String line) {
        if (shouldSkip(line)) {
            return;
        }
        executeNormal(line);
//        switch (state) {
//            case NORMAL -> executeNormal(line);
//            case SEGMENT -> executeSegment(line);
//        }
    }

    private void executeNormal(String line) {
        var instruction = instruction(line);
        var args = args(line);

        switch (instruction) {
            case ADD  -> add(args);
            case SUB  -> sub(args);
            case PUSH -> push(args);
            case POP  -> pop(args);
            case MOV  -> mov(args);
            case MUL  -> mul();
            case UNKNOWN -> System.out.println("unknown instruction: " + line);
        }

//        if (isSegmentStart(line)) {
//            state = State.SEGMENT;
//            lastSegmentStart = instruction(line);
//        }
    }

    private void add(List<String> args) {
        var reg = args.get(0);
        memory.add(reg);
    }

    private void sub(List<String> args) {
        var reg = args.get(0);
        memory.sub(reg);
    }

    private void push(List<String> args) {
        var reg = args.get(0);
        memory.push(reg);
    }

    private void pop(List<String> args) {
        var reg = args.get(0);
        memory.pop(reg);
    }

    private void mov(List<String> args) {
        var to = args.get(0);
        var from = args.get(1); // TODO translate to address
        switch (to) {
            case "ax" -> memory.storeAx((short) 0);
            case "dx" -> memory.storeDx((short) 1);
            default -> {
                throw new Error("invalid register: " + to);
            }
        }
    }

    private void mul() {
        memory.mul();
    }

    private void executeSegment(String line) {
        if (isSegmentEnd(line)) {
            lastSegmentStart = "";
            state = State.NORMAL;
        }
    }

    private ArrayList<String> clean(ArrayList<String> program) {
        var result = new ArrayList<String>();

        for (String line : program) {
            line = line.split(";")[0].trim().toLowerCase();
            if (line.isEmpty()) continue;

            line = line.replaceAll("\s+", " ");

            result.add(line);
        }

        return result;
    }

    private boolean isSegmentStart(String line) {
        return line.toLowerCase().endsWith("segment");
    }

    private boolean isSegmentEnd(String line) {
        return line.endsWith("ends") && line.startsWith(lastSegmentStart);
    }

    private boolean isLabel(String line) {
        return line.endsWith(":");
    }

    private boolean isLabelEnd(String line) {
        return line.toLowerCase().startsWith("end ");
    }

    private boolean shouldSkip(String line) {
        return isSegmentStart(line) || isSegmentEnd(line) || isLabel(line) || isLabelEnd(line) || line.startsWith("assume");
    }

    private Instruction instruction(String line) {
        var instructionString = line.split(" ")[0].trim();

        return switch (instructionString) {
            case "add" -> Instruction.ADD;
            case "sub" -> Instruction.SUB;
            case "mul" -> Instruction.MUL;
            case "mov" -> Instruction.MOV;
            case "push" -> Instruction.PUSH;
            case "pop" -> Instruction.POP;
            default -> Instruction.UNKNOWN;
        };
    }

    private List<String> args(String line) {
        var argString = line.split(" ")[1];

        return Arrays.stream(argString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
