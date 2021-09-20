import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MacroProcessor {
    private final String inputFileName;
    private final HashMap<String, Macro> macroTable = new HashMap<>();
    private State state = State.NORMAL;

    public MacroProcessor(String inputFile) {
        this.inputFileName = inputFile;
    }

    public static void main(String[] args) throws IOException {
        var inputFile = args[0];
        new MacroProcessor(inputFile).processMacro();
    }

    private void processMacro() throws IOException {
        var lines = new ArrayList<String>();

        var reader = getInputReader();
        var macroBuilder = new MacroBuilder();
        var previousState = state;
        String line;

        while ((line = reader.readLine()) != null) {
            previousState = state;
            state = newStateFromLine(line);

            switch (state) {
                case DEFINITION:
                    if (previousState == State.NORMAL) {
                        macroBuilder = new MacroBuilder();
                    }
                    macroBuilder.parseLine(line);
                    break;
                case NORMAL:
                    if (previousState == State.DEFINITION) {
                        storeMacro(macroBuilder.build());
                    }
                    break;
                case EXPANSION:
                    line = expandMacro(line);
                    state = State.NORMAL;
                    break;
            }

            lines.add(line);
        }

        reader.close();

        writeOutput(lines);
    }

    private State newStateFromLine(String line) {
        if (hasMacroDefinition(line)) return State.DEFINITION;
        if (hasMacroEnd(line)) return State.NORMAL;
        if (hasMacroCall(line)) return State.EXPANSION;

        return state;
    }

    private BufferedReader getInputReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(inputFileName));
    }

    private void writeOutput(ArrayList<String> programOut) throws IOException {
        FileWriter fileWriter = new FileWriter("out" + inputFileName);
        for (String line : programOut) {
            fileWriter.write(line + "\n");
        }
        fileWriter.close();
    }

    private boolean hasMacroEnd(String line) {
        return line.contains(Token.MCEND.toString());
    }

    private boolean hasMacroDefinition(String line) {
        return line.contains(Token.MCDEFN.toString());
    }

    private boolean hasMacroCall(String line) {
        var macroName = line.split(" ")[0];
        return macroTable.containsKey(macroName);
    }

    private void storeMacro(Macro macro) {
        macroTable.put(macro.getName(), macro);
    }

    private List<String> parseMacroCall(String line) {
        return Arrays.stream(line.split("[\s,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String expandMacro(String lineWithTheCall) {
        var nameAndArgs = parseMacroCall(lineWithTheCall);
        var name = nameAndArgs.get(0);
        var args = nameAndArgs.subList(1, nameAndArgs.size());

        return macroTable.get(name).expand(args);
    }

    enum State {NORMAL, DEFINITION, EXPANSION}
}
