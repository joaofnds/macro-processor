import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Program {
    Memory memory = new Memory();
    ArrayList<SymbolTableEntry> symbolTable = new ArrayList<>();
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
            storeLineSymbols(line);
           
            if (isDefiningASymbol(line)) continue;

            storeOp(line);
            updateArgSymbols(line);

            locationCounter++;
        }
    }

    private void storeOp(ParsedLine line) {
        var op = new Op();
        op.op = line.instruction;

        for (var arg : line.args) {
            if (hasSymbol(arg)) {
                op.args.add(getSymbol(arg).getValue());
            } else if (isKnownRegister(arg)) {
                op.args.add(knownRegisters.get(arg));
            } else if (isNumeric(arg)) {
                op.args.add(Short.parseShort(arg));
            }
        }

        ops.add(op);
    }

    private void updateArgSymbols(ParsedLine line) {
        line.args.forEach(symbol -> {
            if (!shouldUpdateArgSymbol(symbol)) return;

            var s = getSymbol(symbol);
            if (s.isUndefined()) s.setValue(locationCounter);
        });
    }

    private void storeLineSymbols(ParsedLine line) {
        if (line.hasLabel()) defineLabel(line.label);

        if (isDefiningASymbol(line)) {
            var symbol = getSymbol(line.instruction);
            var value = Short.parseShort(line.args.get(0));

            if (symbol == null) {
                putSymbol(new SymbolTableEntry(line.instruction, value));
            } else {
                defineAndLink(symbol, value);
            }
        }

        line.args.forEach(symbol -> {
            if (shouldStoreArgSymbol(symbol)) putSymbol(new SymbolTableEntry(symbol));
        });
    }

    private boolean shouldStoreArgSymbol(String symbol) {
        return isSymbol(symbol) && !hasSymbol(symbol);
    }

    private boolean shouldUpdateArgSymbol(String symbol) {
        return isSymbol(symbol) && hasSymbol(symbol);
    }

    private boolean isSymbol(String contender) {
        return !isNumeric(contender) && !isKnownRegister(contender);
    }

    private boolean isDefiningASymbol(ParsedLine line) {
        return !isKnowndOp(line.instruction);
    }

    private boolean hasSymbol(String symbol) {
        for (var entry : symbolTable) {
            if (entry.name.equals(symbol)) return true;
        }

        return false;
    }

    private SymbolTableEntry getSymbol(String symbol) {
        for (var s : symbolTable) {
            if (s.name.equals(symbol)) return s;
        }

        return null;
    }

    private short symbolOffset(SymbolTableEntry symbol) {
        for (short i = 0; i < symbolTable.size(); i++) {
            if (symbolTable.get(i) == symbol) return i;
        }

        return -1;
    }

    private void putSymbol(SymbolTableEntry s) {
        symbolTable.add(s);
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
        if (hasSymbol(name)) {
            getSymbol(name).setMultiplyDefined();
            return;
        }

        var entry = new SymbolTableEntry(name);
        putSymbol(entry);
    }

    private void defineAndLink(SymbolTableEntry symbol, short definingValue) {
        short offset = symbolOffset(symbol);
        short lastRef = symbol.getValue();
        for (short i = lastRef; i >= 0; i--) {
            if (i != lastRef) break;

            var op = ops.get(i);

            lastRef = op.args.get(0);
            op.args.set(0, offset);
        }

        symbol.setDefined();
        symbol.setValue(definingValue);
    }

    private boolean isKnowndOp(String op) {
        return directiveTable.containsKey(op);
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
