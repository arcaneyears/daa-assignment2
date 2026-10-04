package dsa;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Path csv = Path.of("results", "results.csv");
        if (args.length == 0 || !args[0].equals("plots")) {
            Benchmark.run(csv);
        }
        Plots.run(csv, Path.of("results", "plots"));
        System.out.println("results -> " + csv.toAbsolutePath());
    }
}
