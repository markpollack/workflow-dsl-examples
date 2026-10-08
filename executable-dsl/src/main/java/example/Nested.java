package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.StepContext;
import io.github.markpollack.workflow.flows.Workflows;
import static io.github.markpollack.workflow.flows.compiler.WorkflowModel.Terminal.SUCCEEDED;

/** The parent sees the child's declared result and its own earlier request. */
public final class Nested {
    private Nested() {}
    public record Request(String repository) {}
    public record Diff(String text) {}
    public record Assessment(boolean acceptable) {}
    public record ReportInput(Request request, Assessment assessment) {}
    public record Report(String repository, boolean acceptable) {}

    public static void run(Path directory) {
        var fetch = new Fetch();
        var assess = new Assess("TODO");
        var report = new WriteReport();
        var reusable = Workflows.define("assess-diff").then(assess).terminate(SUCCEEDED).build();
        var workflow = Workflows.define("nested-review")
                .then(fetch).subWorkflow("quality", reusable)
                .then(report).terminate(SUCCEEDED).build();
        ExampleRunner.success(directory.resolve("nested"), "pr-42", workflow,
                Map.of("fetch", fetch, "assess", assess, "report", report), new Request("widgets"),
                new Report("widgets", false));
    }

    public static final class Fetch implements Step<Request, Diff> {
        public Diff execute(StepContext context, Request input) { return new Diff("TODO: add coverage"); }
    }
    public static final class Assess implements Step<Diff, Assessment> {
        private final String forbidden;
        public Assess(String forbidden) { this.forbidden = forbidden; }
        public Assessment execute(StepContext context, Diff input) {
            return new Assessment(!input.text().contains(forbidden));
        }
    }
    public static final class WriteReport implements Step<ReportInput, Report> {
        public Report execute(StepContext context, ReportInput input) {
            return new Report(input.request().repository(), input.assessment().acceptable());
        }
    }
}
