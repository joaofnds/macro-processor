import java.util.ArrayList;
import java.util.List;

public class IntermediateFile {
    ArrayList<SymbolTableEntry> symbolTable;
    List<Instruction> instructions;

    public IntermediateFile(ArrayList<SymbolTableEntry> symbolTable, List<Instruction> instructions) {
        this.symbolTable = symbolTable;
        this.instructions = instructions;
    }
}
