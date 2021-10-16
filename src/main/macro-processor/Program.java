import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Program {
    Memory memory = new Memory();
    HashMap<String, SymbolTableEntry> symbolTable = new HashMap<>();
    Map<String, OpCode> directiveTable = Arrays.stream(
            new OpCode[]{
                    new OpCode("add", (byte) 0, OpCode.Type.RegisterRegister),
                    new OpCode("sub", (byte) 1, OpCode.Type.RegisterRegister),
                    new OpCode("push", (byte) 2, OpCode.Type.RegisterRegister),
                    new OpCode("pop", (byte) 3, OpCode.Type.RegisterRegister),
                    new OpCode("beq", (byte) 4, OpCode.Type.RegisterRegister),
                    new OpCode("bne", (byte) 5, OpCode.Type.RegisterRegister),
                    new OpCode("jmp", (byte) 6, OpCode.Type.RegisterRegister),
            }
    ).collect(Collectors.toMap(o -> o.mnemonic, o -> o));
    short locationCounter = 0; // LC
    HashMap<String, Short> knownRegisters = new HashMap<>() {{
        put("ax", (short) 0b11110000);
        put("dx", (short) 0b11110001);
    }};
    List<Op> ops = new ArrayList<>();

    public Program(String inputFile) throws IOException {
        var program = expandMacro(inputFile);
        var lines = clean(program).stream().map(ParsedLine::fromLine).collect(Collectors.toList());

        firstPass(lines);
        secondPass(lines);
    }

    public static void main(String[] args) throws IOException {
        var inputFile = "src/main/resources/program.asm";
        new Program(inputFile);
    }

    private void firstPass(List<ParsedLine> lines) {
        locationCounter = 0;

        for (var line : lines) {
            // ensure symbols
            if (line.hasLabel()) defineLabel(line.label);

            if (!isKnowndOp(line.instruction) && isKnownSymbol(line.instruction))
                defineAndLink(symbolTable.get(line.instruction), Short.parseShort(line.args.get(0)));

            line.args.forEach(symbol -> {
                if (isNumeric(symbol)) return;
                if (isKnownRegister(symbol)) return;
                if (symbolTable.containsKey(symbol)) return;

                symbolTable.put(symbol, new SymbolTableEntry(symbol));
            });

            // store op
            if (!isKnowndOp(line.instruction)) return;

            var op = new Op();
            op.op = line.instruction;

            for (var arg : line.args) {
                if (symbolTable.containsKey(arg)) {
                    op.args.add(symbolTable.get(arg).getValue());
                } else if (isKnownRegister(arg)) {
                    op.args.add(knownRegisters.get(arg));
                } else if (isNumeric(arg)) {
                    op.args.add(Short.parseShort(arg));
                }
            }

            ops.add(op);

            line.args.forEach(symbol -> {
                if (isNumeric(symbol)) return;
                if (isKnownRegister(symbol)) return;
                if (!symbolTable.containsKey(symbol)) return;

                var s = symbolTable.get(symbol);
                if (s.isUndefined()) {
                    s.setValue(locationCounter);
                }
            });

            locationCounter++;
        }
    }

    private void secondPass(List<ParsedLine> lines) {
        locationCounter = 0;
        for (var line : lines) {
//            execute(line);
            locationCounter++;
        }
    }

    private ArrayList<String> expandMacro(String inputFile) throws IOException {
        return new MacroProcessor(inputFile).processMacro();
    }

    private void defineLabel(String name) {
        if (symbolTable.containsKey(name)) {
            symbolTable.get(name).setMultiplyDefined();
            return;
        }

        var entry = new SymbolTableEntry(name);
        symbolTable.put(name, entry);
    }

    private void defineAndLink(SymbolTableEntry symbol) {
        short value = symbolTable.
        short lastRef = symbol.getValue();
        for (short i = lastRef; i >= 0; i--) {
            if (i != lastRef) break;

            var op = ops.get(i);

            lastRef = op.args.get(0);
            op.args.set(0, value);
        }

        symbol.setDefined();
        symbol.setValue(value);
    }

    private boolean isKnowndOp(String op) {
        return directiveTable.containsKey(op);
    }

    private boolean isKnownSymbol(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    private boolean isKnownRegister(String name) {
        return knownRegisters.containsKey(name);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

//    private void add(List<String> args) {
//        var reg = args.get(0);
//        memory.add(reg);
//    }
//
//    private void sub(List<String> args) {
//        var reg = args.get(0);
//        memory.sub(reg);
//    }
//
//    private void push(List<String> args) {
//        var reg = args.get(0);
//        memory.push(reg);
//    }
//
//    private void pop(List<String> args) {
//        var reg = args.get(0);
//        memory.pop(reg);
//    }
//
//    private void mov(List<String> args) {
//        var to = args.get(0);
//        var from = args.get(1); // TODO translate to address
//        switch (to) {
//            case "ax" -> memory.storeAx((short) 0);
//            case "dx" -> memory.storeDx((short) 1);
//            default -> {
//                throw new Error("invalid register: " + to);
//            }
//        }
//    }
//
//    private void mul() {
//        memory.mul();
//    }

    private ArrayList<String> clean(ArrayList<String> program) {
        var result = new ArrayList<String>();

        for (String line : program) {
            line = line.trim().toLowerCase();
            if (line.isEmpty()) continue;

            line = line.replaceAll("\s+", " ");

            result.add(line);
        }

        return result;
    }

    class Op {
        String op;
        ArrayList<Short> args = new ArrayList<>();

        @Override
        public String toString() {
            return op + ": " + args.get(0);
        }
    }
}
