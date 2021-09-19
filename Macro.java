import java.util.List;

public class Macro {
    private final String name;
    private final List<String> args;
    private final List<String> body;

    public Macro(String name, List<String> args, List<String> body) {
        this.name = name;
        this.args = args;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String expand(List<String> params) {
        if (args.size() != params.size()) {
            throw new IllegalArgumentException("want a list of " + args.size());
        }

        var out = String.join("\n", body);

        for (int i = 0; i < args.size(); i++) {
            out = out.replaceAll(args.get(i), params.get(i));
        }

        return out;
    }
}
