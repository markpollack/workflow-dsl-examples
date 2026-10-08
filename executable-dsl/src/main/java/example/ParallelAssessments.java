package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.StepContext;
import io.github.markpollack.workflow.flows.Workflows;
import static io.github.markpollack.workflow.flows.compiler.WorkflowModel.Terminal.SUCCEEDED;

/** Different assessment types form a domain record, without tuple casts. */
public final class ParallelAssessments {
    private ParallelAssessments() {}
    public record Request(String repository) {}
    public record Diff(String revision) {}
    public record Quality(boolean acceptable) {}
    public record Backport(boolean suitable) {}
    public record BackportInput(Request request, Diff diff) {}
    public record ReportInput(Request request, Quality quality, Backport backport) {}
    public record Report(String repository, boolean acceptable, boolean suitable) {}

    public static void run(Path directory) {
        var fetch = new Fetch();
        var quality = new AssessQuality();
        var backport = new AssessBackport();
        var report = new WriteReport();
        var workflow = Workflows.define("parallel-review")
                .then(fetch).parallel("assessments").allSuccessful()
                    .branch("quality").then(quality)
                    .branch("backport").then(backport)
                .end().then(report).terminate(SUCCEEDED).build();
        ExampleRunner.success(directory.resolve("parallel"), "pr-42", workflow,
                Map.of("fetch", fetch, "quality", quality, "backport", backport, "report", report),
                new Request("widgets"), new Report("widgets", false, true));
    }

    public static final class Fetch implements Step<Request, Diff> {
        public Diff execute(StepContext context, Request input) { return new Diff("revision-17"); }
    }
    public static final class AssessQuality implements Step<Diff, Quality> {
        public Quality execute(StepContext context, Diff input) { return new Quality(false); }
    }
    public static final class AssessBackport implements Step<BackportInput, Backport> {
        public Backport execute(StepContext context, BackportInput input) { return new Backport(true); }
    }
    public static final class WriteReport implements Step<ReportInput, Report> {
        public Report execute(StepContext context, ReportInput input) {
            return new Report(input.request().repository(), input.quality().acceptable(), input.backport().suitable());
        }
    }
}
