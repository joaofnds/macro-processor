import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacroBuilder {
    private final ArrayList<String> lines = new ArrayList<>();
    private List<String> args = new ArrayList<>();
    private String name;


    void parseLine(String line) {
        if (line.contains(Token.MCDEFN.toString())) {
            parseDefinition(line);
        } else {
            parseBody(line);
        }
    }

    Macro build() {
        return new Macro(this.name, this.args, this.lines);
    }

    private void parseDefinition(String line) {
        line = removeSubsequentSpaces(line);
        this.name = definitionName(line);
        this.args = definitionArgs(line);
    }

    private void parseBody(String line) {
        this.lines.add(line);
    }

    private String definitionName(String line) {
        String[] words = line.split(Token.MCDEFN.toString());
        return words[0].trim();
    }


    private List<String> definitionArgs(String line) {
        line = line.split(";")[0];
        String[] words = line.split(Token.MCDEFN.toString());

        return Arrays.stream(words[1].split(","))
                .map(String::trim)
                .toList();
    }

    private String removeSubsequentSpaces(String str) {
        return str.replaceAll("\\s+", " ");
    }
}
