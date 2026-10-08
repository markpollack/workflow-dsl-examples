package example;

import java.nio.file.Path;

/** Run all six lessons, or run them again with the same directory to reuse committed work. */
public final class Progression {
    private Progression() {}
    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Expected: <store-directory>");
        var directory = Path.of(args[0]);
        Sequential.run(directory);
        Nested.run(directory);
        EnumDecision.run(directory);
        NativeVerdict.run(directory);
        ParallelAssessments.run(directory);
        FanOut.run(directory);
        System.out.println("Six lessons passed; no provider calls.");
    }
}
