package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.StepContext;
import io.github.markpollack.workflow.flows.Workflows;
import static io.github.markpollack.workflow.flows.compiler.WorkflowModel.Terminal.SUCCEEDED;

/** An enum is routing evidence; it does not replace the branch's business input. */
public final class EnumDecision {
    private EnumDecision() {}
    public record Request(boolean documentationOnly) {}
    public enum Route { QUICK, FULL }
    public record Report(String text) {}

    public static void run(Path directory) {
        var choose = new Choose();
        var quick = new Review("quick review");
        var full = new Review("full review");
        var workflow = Workflows.define("enum-review")
                .decision("review-depth", choose)
                    .when(Route.QUICK).then(quick)
                    .when(Route.FULL).then(full)
                .end().terminate(SUCCEEDED).build();
        Map<String, Step<?, ?>> steps = Map.of("choose", choose, "quick", quick, "full", full);
        ExampleRunner.success(directory.resolve("enum"), "docs", workflow, steps,
                new Request(true), new Report("quick review"));
        ExampleRunner.success(directory.resolve("enum"), "code", workflow, steps,
                new Request(false), new Report("full review"));
    }

    public static final class Choose implements Step<Request, Route> {
        public Route execute(StepContext context, Request input) {
            return input.documentationOnly() ? Route.QUICK : Route.FULL;
        }
    }
    public static final class Review implements Step<Request, Report> {
        private final String heading;
        public Review(String heading) { this.heading = heading; }
        public Report execute(StepContext context, Request input) { return new Report(heading); }
    }
}
