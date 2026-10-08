package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.StepContext;
import io.github.markpollack.workflow.flows.Workflows;
import static io.github.markpollack.workflow.flows.compiler.WorkflowModel.Terminal.SUCCEEDED;

/** Two Steps; the second combines the first output with the original request. */
public final class Sequential {
    private Sequential() {}
    public record Request(String repository, int number) {}
    public record Diff(String revision, String text) {}
    public record ReportInput(Request request, Diff diff) {}
    public record Report(String text) {}

    public static void run(Path directory) {
        var fetch = new Fetch("revision-17");
        var report = new WriteReport();
        var workflow = Workflows.define("sequential-review")
                .then(fetch).then(report).terminate(SUCCEEDED).build();
        ExampleRunner.success(directory.resolve("sequential"), "pr-42", workflow,
                Map.of("fetch", fetch, "report", report), new Request("widgets", 42),
                new Report("widgets#42 revision-17"));
    }

    public static final class Fetch implements Step<Request, Diff> {
        private final String revision;
        public Fetch(String revision) { this.revision = revision; }
        public Diff execute(StepContext context, Request input) {
            return new Diff(revision, "Add coverage");
        }
    }

    public static final class WriteReport implements Step<ReportInput, Report> {
        public Report execute(StepContext context, ReportInput input) {
            return new Report(input.request().repository() + "#" + input.request().number()
                    + " " + input.diff().revision());
        }
    }
}
