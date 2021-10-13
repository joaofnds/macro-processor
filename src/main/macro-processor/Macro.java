import java.util.List;

public class Macro {
    private final String name;
    private final List<String> params;
    private final List<String> body;

    public Macro(String name, List<String> args, List<String> body) {
        this.name = name;
        this.params = args;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String expand(List<String> args) {
        if (params.size() != args.size()) {
            throw new IllegalArgumentException(
                    "want a list of size " + params.size() + ", given: " + String.join(",", args)
            );
        }

        var out = String.join("\n", body);

        for (int i = 0; i < params.size(); i++) {
            out = out.replaceAll(params.get(i), args.get(i));
        }

        return out;
    }
}
