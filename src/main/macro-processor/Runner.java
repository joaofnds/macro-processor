import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Runner {
    public static void main(String[] args) throws IOException {
        String inputFile = "src/main/resources/program.asm";
        var program = new MacroProcessor(inputFile).processMacro();
        var lines = clean(program).stream().map(ParsedLine::fromLine).collect(Collectors.toList());

        Program p = new Program();
        var intermediateFile = p.firstPass(lines);
        p.secondPass(intermediateFile);
    }

    private static ArrayList<String> clean(ArrayList<String> program) {
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
