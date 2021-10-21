import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static utils.NumberUtils.isNumeric;

public class Program {
    static final OpCode ADDRR = new OpCode("add",   (byte) 0,  OpCode.Type.RegisterRegister);
    static final OpCode ADDRX = new OpCode("add",   (byte) 1,  OpCode.Type.RegisterIndex);
    static final OpCode ANDRR = new OpCode("and",   (byte) 8,  OpCode.Type.RegisterRegister);
    static final OpCode ANDRX = new OpCode("and",   (byte) 9,  OpCode.Type.RegisterIndex);
    static final OpCode BEQRR = new OpCode("beq",   (byte) 4,  OpCode.Type.RegisterRegister);
    static final OpCode BEQRI = new OpCode("beq",   (byte) 4,  OpCode.Type.RegisterIndex);
    static final OpCode BNERR = new OpCode("bne",   (byte) 5,  OpCode.Type.RegisterRegister);
    static final OpCode BNERI = new OpCode("bne",   (byte) 5,  OpCode.Type.RegisterIndex);
    static final OpCode CALL  = new OpCode("call",  (byte) 19, OpCode.Type.RegisterIndex);
    static final OpCode CMPRR = new OpCode("cmp",   (byte) 6,  OpCode.Type.RegisterRegister);
    static final OpCode CMPRX = new OpCode("cmp",   (byte) 7,  OpCode.Type.RegisterIndex);
    static final OpCode DIV   = new OpCode("div",   (byte) 2,  OpCode.Type.RegisterRegister);
    static final OpCode HLT   = new OpCode("hlt",   (byte) 21, OpCode.Type.RegisterRegister);
    static final OpCode JMPRR = new OpCode("jmp",   (byte) 6,  OpCode.Type.RegisterRegister);
    static final OpCode JMPRI = new OpCode("jmp",   (byte) 6,  OpCode.Type.RegisterIndex);
    static final OpCode JNZ   = new OpCode("jnz",   (byte) 17, OpCode.Type.RegisterIndex);
    static final OpCode JP    = new OpCode("jp",    (byte) 18, OpCode.Type.RegisterIndex);
    static final OpCode JZ    = new OpCode("jz",    (byte) 16, OpCode.Type.RegisterIndex);
    static final OpCode MUL   = new OpCode("mul",   (byte) 5,  OpCode.Type.RegisterRegister);
    static final OpCode NOT   = new OpCode("not",   (byte) 10, OpCode.Type.RegisterIndex);
    static final OpCode ORRR  = new OpCode("or",    (byte) 11, OpCode.Type.RegisterRegister);
    static final OpCode ORRX  = new OpCode("or",    (byte) 12, OpCode.Type.RegisterIndex);
    static final OpCode POP   = new OpCode("pop",   (byte) 3,  OpCode.Type.RegisterRegister);
    static final OpCode POPF  = new OpCode("popf",  (byte) 24, OpCode.Type.RegisterRegister);
    static final OpCode POPRR = new OpCode("pop",   (byte) 22, OpCode.Type.RegisterRegister);
    static final OpCode POPRX = new OpCode("pop",   (byte) 23, OpCode.Type.RegisterIndex);
    static final OpCode PUSH  = new OpCode("push",  (byte) 2,  OpCode.Type.RegisterRegister);
    static final OpCode PUSHF = new OpCode("pushf", (byte) 26, OpCode.Type.RegisterRegister);
    static final OpCode READ  = new OpCode("read",  (byte) 28, OpCode.Type.RegisterIndex);
    static final OpCode RET   = new OpCode("ret",   (byte) 20, OpCode.Type.RegisterRegister);
    static final OpCode STORE = new OpCode("store", (byte) 27, OpCode.Type.RegisterRegister);
    static final OpCode SUB   = new OpCode("sub",   (byte) 1,  OpCode.Type.RegisterRegister);
    static final OpCode SUBRR = new OpCode("sub",   (byte) 3,  OpCode.Type.RegisterRegister);
    static final OpCode SUBRX = new OpCode("sub",   (byte) 4,  OpCode.Type.RegisterIndex);
    static final OpCode WRITE = new OpCode("write", (byte) 29, OpCode.Type.RegisterIndex);
    static final OpCode XORRI = new OpCode("xor",   (byte) 14, OpCode.Type.RegisterImmediate);
    static final OpCode XORRR = new OpCode("xor",   (byte) 13, OpCode.Type.RegisterRegister);

    private final OpCode[] directives = {
            ADDRR, ADDRX, ANDRR, ANDRX, BEQRR, BEQRI, BNERR, BNERI, CALL, CMPRR, CMPRX, DIV,
            HLT, JMPRR, JMPRI, JNZ, JP, JZ, MUL, NOT, ORRR, ORRX, POP, POPF, POPRR, POPRX,
            PUSH, PUSHF, READ, RET, STORE, SUB, SUBRR, SUBRX, WRITE, XORRI, XORRR,
    };

    Memory memory = new Memory();
    ArrayList<SymbolTableEntry> symbolTable = new ArrayList<>();

    HashMap<String, Short> registers = new HashMap<>() {{
        put("ax", (short) 0b11110000);
        put("dx", (short) 0b11110001);
    }};
    List<Instruction> instructions = new ArrayList<>();

    short locationCounter = 0; // LC

    public Program(String inputFile) throws IOException {
        var program = expandMacro(inputFile);
        var lines = clean(program).stream().map(ParsedLine::fromLine).collect(Collectors.toList());

        firstPass(lines);
        secondPass(instructions);
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
        var op = new Instruction();
        op.op = line.instruction;
        op.type = opType(line.args.get(0));

        line.args.forEach(arg -> op.args.add(parseArg(arg)));

        instructions.add(op);
    }

    private short parseArg(String arg) {
        return switch (opType(arg)) {
            case RegisterRegister -> registers.get(arg);
            case RegisterIndex -> getSymbol(arg).getValue();
            case RegisterImmediate -> Short.parseShort(arg);
        };
    }

    private OpCode.Type opType(String arg) {
        if (isNumeric(arg)) {
            return OpCode.Type.RegisterImmediate;
        } else if (isKnownRegister(arg)) {
            return OpCode.Type.RegisterRegister;
        } else {
            return OpCode.Type.RegisterIndex;
        }
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
        return !isKnownOp(line.instruction);
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

    private void secondPass(List<Instruction> instructions) {
        locationCounter = 0;
        for (var op : instructions) {
            execute(op);
            locationCounter++;
        }
    }

    private OpCode findOpCode(Instruction instruction) {
        return Arrays.stream(directives)
                .filter(d -> d.mnemonic.equals(instruction.op) && d.type == instruction.type)
                .findFirst()
                .orElse(null);
    }

    private void execute(Instruction instruction) {
        OpCode opCode = findOpCode(instruction);

        if (opCode == null) throw new OpCodeNotFoundError(instruction);

        if (ADDRR.equals(opCode)) {
            memory.add(instruction.args.get(0));
        } else if (ADDRX.equals(opCode)) {
            memory.add(registers.get("rx"));
        } else if (ANDRR.equals(opCode)) {
        } else if (ANDRX.equals(opCode)) {
            memory.and(registers.get("rx"));
        } else if (BEQRR.equals(opCode)) {
        } else if (BEQRI.equals(opCode)) {
        } else if (BNERR.equals(opCode)) {
        } else if (BNERI.equals(opCode)) {
        } else if (CALL.equals(opCode)) {
        } else if (CMPRR.equals(opCode)) {
        } else if (CMPRX.equals(opCode)) {
            memory.cmp(registers.get("rx"));
        } else if (DIV.equals(opCode)) {
        } else if (HLT.equals(opCode)) {
        } else if (JMPRR.equals(opCode)) {
        } else if (JMPRI.equals(opCode)) {
        } else if (JNZ.equals(opCode)) {
        } else if (JP.equals(opCode)) {
        } else if (JZ.equals(opCode)) {
        } else if (MUL.equals(opCode)) {
        } else if (NOT.equals(opCode)) {
        } else if (ORRR.equals(opCode)) {
        } else if (ORRX.equals(opCode)) {
        } else if (POP.equals(opCode)) {
        } else if (POPF.equals(opCode)) {
        } else if (POPRR.equals(opCode)) {
        } else if (POPRX.equals(opCode)) {
        } else if (PUSH.equals(opCode)) {
        } else if (PUSHF.equals(opCode)) {
        } else if (READ.equals(opCode)) {
        } else if (RET.equals(opCode)) {
        } else if (STORE.equals(opCode)) {
        } else if (SUB.equals(opCode)) {
        } else if (SUBRR.equals(opCode)) {
        } else if (SUBRX.equals(opCode)) {
        } else if (WRITE.equals(opCode)) {
        } else if (XORRI.equals(opCode)) {
        } else if (XORRR.equals(opCode)) {
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

            var op = instructions.get(i);

            lastRef = op.args.get(0);
            op.args.set(0, offset);
        }

        symbol.setDefined();
        symbol.setValue(definingValue);
    }

    private boolean isKnownOp(String op) {
        return Arrays.stream(directives).anyMatch(d -> d.mnemonic.equals(op));
    }

    private boolean isKnownRegister(String name) {
        return registers.containsKey(name);
    }

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

}
