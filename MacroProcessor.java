import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MacroProcessor {
    private final String inputFileName;
    private final HashMap<String, Macro> macros = new HashMap<>();
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

        var reader = getInputFileReader();
        String line;
        var macroBuilder = new MacroBuilder();
        var previousState = state;

        while ((line = reader.readLine()) != null) {
            line = removeSubsequentSpaces(line);

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
                        var macro = macroBuilder.build();
                        macros.put(macro.getName(), macro);
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

    private BufferedReader getInputFileReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(inputFileName));
    }

    private void writeOutput(ArrayList<String> macro) throws IOException {
        FileWriter fileWriter = new FileWriter(inputFileName);
        for (String line : macro) {
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
        if (!line.trim().contains(" ")) {
            return false;
        }

        var names = macros.values().stream().map(Macro::getName).toList();
        var firstToken = line.split(" ")[0];
        return names.contains(firstToken);
    }

    private List<String> parseMacroCall(String line) {
        return Arrays.stream(line.split("[\s,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }


    private String removeSubsequentSpaces(String str) {
        return str.replaceAll("\\s+", " ");
    }

    private String expandMacro(String lineWithTheCall) {
        var nameAndParams = parseMacroCall(lineWithTheCall);
        var name = nameAndParams.get(0);
        var params = nameAndParams.subList(1, nameAndParams.size());

        return macroTable.get(name).expand(params);
    }

    enum State {NORMAL, DEFINITION, EXPANSION}
}
