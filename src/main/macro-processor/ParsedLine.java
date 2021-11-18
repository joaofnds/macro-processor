import java.util.ArrayList;

class ParsedLine {
    String label;
    String instruction;
    ArrayList<String> args = new ArrayList<>();
    String comment;

    static ParsedLine fromLine(String line) {
        var parsedLine = new ParsedLine();

        boolean hasComment = line.contains(";");
        if (hasComment) {
            var lineAndComment = line.split(";");
            parsedLine.comment = lineAndComment[1].strip();
            line = lineAndComment[0].strip();
        }

        boolean hasLabel = line.contains(":");
        if (hasLabel) {
            var labelAndInst = line.split(":");
            parsedLine.label = labelAndInst[0].strip();
            line = labelAndInst[1].strip();
        }

        var indexOfFirstSpace = line.strip().indexOf(" ");

        boolean hasNoArgs = indexOfFirstSpace == -1;
        if (hasNoArgs) {
            parsedLine.instruction = line;
            return parsedLine;
        }


        parsedLine.instruction = line.substring(0, indexOfFirstSpace).strip();

        for (var arg : line.substring(indexOfFirstSpace).split(",")) {
            parsedLine.args.add(arg.strip());
        }

        return parsedLine;
    }

    public boolean hasLabel() {
        return label != null && !label.isEmpty();
    }
}
