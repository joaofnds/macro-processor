import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static utils.NumberUtils.isNumeric;

public class Program {
    static final OpCode HLT = new OpCode("hlt", (byte) 0, OpCode.Type.RegisterRegister);
    static final OpCode ADDRR = new OpCode("add", (byte) 1, OpCode.Type.RegisterRegister);
    static final OpCode ADDRX = new OpCode("add", (byte) 2, OpCode.Type.RegisterIndex);
    static final OpCode ANDRR = new OpCode("and", (byte) 3, OpCode.Type.RegisterRegister);
    static final OpCode ANDRX = new OpCode("and", (byte) 4, OpCode.Type.RegisterIndex);
    static final OpCode BEQRR = new OpCode("beq", (byte) 5, OpCode.Type.RegisterRegister);
    static final OpCode BEQRI = new OpCode("beq", (byte) 6, OpCode.Type.RegisterIndex);
    static final OpCode BNERR = new OpCode("bne", (byte) 7, OpCode.Type.RegisterRegister);
    static final OpCode BNERI = new OpCode("bne", (byte) 8, OpCode.Type.RegisterIndex);
    static final OpCode CALL = new OpCode("call", (byte) 9, OpCode.Type.RegisterIndex);
    static final OpCode CMPRR = new OpCode("cmp", (byte) 10, OpCode.Type.RegisterRegister);
    static final OpCode CMPRX = new OpCode("cmp", (byte) 11, OpCode.Type.RegisterIndex);
    static final OpCode DIV = new OpCode("div", (byte) 12, OpCode.Type.RegisterRegister);
    static final OpCode JMPRR = new OpCode("jmp", (byte) 13, OpCode.Type.RegisterRegister);
    static final OpCode JMPRX = new OpCode("jmp", (byte) 14, OpCode.Type.RegisterIndex);
    static final OpCode JNZ = new OpCode("jnz", (byte) 15, OpCode.Type.RegisterIndex);
    static final OpCode JP = new OpCode("jp", (byte) 16, OpCode.Type.RegisterIndex);
    static final OpCode JZ = new OpCode("jz", (byte) 17, OpCode.Type.RegisterIndex);
    static final OpCode MUL = new OpCode("mul", (byte) 18, OpCode.Type.RegisterRegister);
    static final OpCode NOT = new OpCode("not", (byte) 19, OpCode.Type.RegisterIndex);
    static final OpCode ORRR = new OpCode("or", (byte) 20, OpCode.Type.RegisterRegister);
    static final OpCode ORRX = new OpCode("or", (byte) 21, OpCode.Type.RegisterIndex);
    static final OpCode POP = new OpCode("pop", (byte) 22, OpCode.Type.RegisterRegister);
    static final OpCode POPF = new OpCode("popf", (byte) 23, OpCode.Type.RegisterRegister);
    static final OpCode POPRR = new OpCode("pop", (byte) 24, OpCode.Type.RegisterRegister);
    static final OpCode POPRX = new OpCode("pop", (byte) 25, OpCode.Type.RegisterIndex);
    static final OpCode PUSH = new OpCode("push", (byte) 26, OpCode.Type.RegisterRegister);
    static final OpCode PUSHF = new OpCode("pushf", (byte) 27, OpCode.Type.RegisterRegister);
    static final OpCode READ = new OpCode("read", (byte) 28, OpCode.Type.RegisterIndex);
    static final OpCode RET = new OpCode("ret", (byte) 29, OpCode.Type.RegisterRegister);
    static final OpCode STRRR = new OpCode("store", (byte) 30, OpCode.Type.RegisterRegister);
    static final OpCode STRRX = new OpCode("store", (byte) 31, OpCode.Type.RegisterIndex);
    static final OpCode SUB = new OpCode("sub", (byte) 32, OpCode.Type.RegisterRegister);
    static final OpCode SUBRR = new OpCode("sub", (byte) 33, OpCode.Type.RegisterRegister);
    static final OpCode SUBRX = new OpCode("sub", (byte) 34, OpCode.Type.RegisterIndex);
    static final OpCode SUBRI = new OpCode("sub", (byte) 35, OpCode.Type.RegisterImmediate);
    static final OpCode WRITE = new OpCode("write", (byte) 36, OpCode.Type.RegisterIndex);
    static final OpCode XORRI = new OpCode("xor", (byte) 37, OpCode.Type.RegisterImmediate);
    static final OpCode XORRR = new OpCode("xor", (byte) 38, OpCode.Type.RegisterRegister);

    private final OpCode[] directives = {
            ADDRR, ADDRX, ANDRR, ANDRX, BEQRR, BEQRI, BNERR, BNERI, CALL, CMPRR, CMPRX, DIV,
            HLT, JMPRR, JMPRX, JNZ, JP, JZ, MUL, NOT, ORRR, ORRX, POP, POPF, POPRR, POPRX, PUSH,
            PUSHF, READ, RET, STRRR, STRRX, SUB, SUBRR, SUBRX, SUBRI, WRITE, XORRI, XORRR,
    };

    Memory memory = new Memory();
    ArrayList<SymbolTableEntry> symbolTable = new ArrayList<>();

    HashMap<String, Short> registers = new HashMap<>() {{
        put("ax", Memory.AX);
        put("dx", Memory.DX);
    }};
    List<Instruction> instructions = new ArrayList<>();

    short locationCounter = 0; // LC

    public IntermediateFile firstPass(List<ParsedLine> lines) {
        locationCounter = 0;

        for (var line : lines) {
            storeLineSymbols(line);

            if (isDefiningASymbol(line)) continue;

            storeOp(line);
            updateArgSymbols(line);

            locationCounter++;
        }

        return new IntermediateFile(symbolTable, instructions);
    }

    private void storeOp(ParsedLine line) {
        var op = new Instruction();
        var type = line.args.size() > 0 ? opType(line.args.get(0)) : OpCode.Type.RegisterRegister;
        op.op = findOpCode(line.instruction, type);

        line.args.forEach(arg -> op.args.add(parseArg(arg)));

        instructions.add(op);
    }

    private short parseArg(String arg) {
        return switch (opType(arg)) {
            case RegisterRegister -> registers.get(arg);
            case RegisterIndex -> symbolOffset(getSymbol(arg));
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
                var s = new SymbolTableEntry(line.instruction, value);
                s.setValue(Short.parseShort(line.args.get(0)));
                s.setDefined();
                putSymbol(s);
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

    public void secondPass(IntermediateFile intermediateFile) {
        loadIntoMemory(intermediateFile);
        execute();
    }

    private void loadIntoMemory(IntermediateFile intermediateFile) {
        for (SymbolTableEntry symbol : intermediateFile.symbolTable) {
            memory.storeSymbol(symbol.getValue());
        }

        for (var instruction : intermediateFile.instructions) {
            memory.storeOp(instruction.op.opcode, instruction.args.get(0));
        }

        memory.ip = memory.opStart;
    }

    private OpCode findOpCode(String op, OpCode.Type type) {
        return Arrays.stream(directives)
                .filter(d -> d.mnemonic.equals(op) && d.type == type)
                .findFirst()
                .orElse(null);
    }

    public OpCode findOpByCode(byte opcode) {
        return Arrays.stream(directives)
                .filter(d -> d.opcode == opcode)
                .findFirst()
                .orElse(null);

    }


    public Instruction readInstructionFromMemory() {
        Instruction instruction = new Instruction();
        byte opcode = (byte) memory.readOp();
        instruction.op = findOpByCode(opcode);
        instruction.args = new ArrayList<>();
        instruction.args.add(memory.readOp());
        return instruction;
    }

    private void execute() {
        while (true) {
            Instruction instruction = readInstructionFromMemory();
            if (instruction == null) {
                memory.hlt();
                break;
            } else if (ADDRR.equals(instruction.op)) {
                Short address = instruction.args.get(0);
                memory.add(address);
            } else if (ADDRX.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.add(address);
            } else if (ANDRR.equals(instruction.op)) {
            } else if (ANDRX.equals(instruction.op)) {
                memory.and(registers.get("rx"));
            } else if (BEQRR.equals(instruction.op)) {
            } else if (BEQRI.equals(instruction.op)) {
            } else if (BNERR.equals(instruction.op)) {
            } else if (BNERI.equals(instruction.op)) {
            } else if (CALL.equals(instruction.op)) {
            } else if (CMPRR.equals(instruction.op)) {
            } else if (CMPRX.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.cmp(address);
            } else if (DIV.equals(instruction.op)) {
            } else if (HLT.equals(instruction.op)) {
                memory.hlt();
                break;
            } else if (JMPRR.equals(instruction.op)) {
            } else if (JMPRX.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.jmp(address);
            } else if (JNZ.equals(instruction.op)) {
            } else if (JP.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.jp(address);
            } else if (JZ.equals(instruction.op)) {
            } else if (MUL.equals(instruction.op)) {
            } else if (NOT.equals(instruction.op)) {
            } else if (ORRR.equals(instruction.op)) {
            } else if (ORRX.equals(instruction.op)) {
            } else if (POP.equals(instruction.op)) {
            } else if (POPF.equals(instruction.op)) {
            } else if (POPRR.equals(instruction.op)) {
            } else if (POPRX.equals(instruction.op)) {
            } else if (PUSH.equals(instruction.op)) {
            } else if (PUSHF.equals(instruction.op)) {
            } else if (READ.equals(instruction.op)) {
            } else if (RET.equals(instruction.op)) {
            } else if (STRRR.equals(instruction.op)) {
            } else if (STRRX.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.storeAx(address);
            } else if (SUB.equals(instruction.op)) {
            } else if (SUBRR.equals(instruction.op)) {
            } else if (SUBRX.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.sub(address);
            } else if (WRITE.equals(instruction.op)) {
                short address = memory.symbolAddress(instruction.args.get(0));
                memory.write(address);
            } else if (XORRI.equals(instruction.op)) {
            } else if (XORRR.equals(instruction.op)) {
            }
        }
    }

    private void defineLabel(String name) {
        SymbolTableEntry entry = null;

        if (hasSymbol(name)) {
            entry = getSymbol(name);
        } else {
            entry = new SymbolTableEntry(name);
        }

        entry.setValue(locationCounter);
        entry.setDefined();
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
}
