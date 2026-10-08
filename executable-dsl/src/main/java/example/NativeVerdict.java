package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.StepContext;
import io.github.markpollack.workflow.flows.Workflows;
import static io.github.markpollack.workflow.flows.compiler.WorkflowModel.Terminal.SUCCEEDED;

import java.util.List;
import io.github.markpollack.judge.judgment.Judgment;
import io.github.markpollack.judge.jury.SimpleJury;
import io.github.markpollack.judge.jury.JudgeSeat;
import io.github.markpollack.judge.verdict.Verdict;
import io.github.markpollack.judge.voting.AllEligiblePassStrategy;
import io.github.markpollack.judge.voting.ErrorHandling;
import io.github.markpollack.judge.voting.ExclusionHandling;
import static io.github.markpollack.judge.verdict.Verdict.Conclusion.*;

/** Deterministic judgments through a real native Jury, including ERROR and ABSTAIN. */
public final class NativeVerdict {
    private NativeVerdict() {}
    public record Request(Verdict.Conclusion fixture, boolean throwFailure) {}
    public record ReviewInput(Request request, Verdict assessment, Verdict.Conclusion conclusion) {}
    public record Report(String heading, Verdict.Conclusion conclusion, List<String> evidence) {}

    public static void run(Path directory) {
        var assess = new Assess();
        var passed = new Explain("accepted");
        var failed = new Explain("changes requested");
        var inconclusive = new Explain("needs attention");
        var excluded = new Explain("outside scope");
        var workflow = Workflows.define("native-review")
                .verdict("quality", assess)
                    .when(PASS).then(passed)
                    .when(FAIL).then(failed)
                    .when(INCONCLUSIVE).then(inconclusive)
                    .when(NOT_APPLICABLE).then(excluded)
                .end().terminate(SUCCEEDED).build();
        Map<String, Step<?, ?>> steps = Map.of("assess", assess, "passed", passed, "failed", failed,
                "inconclusive", inconclusive, "excluded", excluded);
        ExampleRunner.success(directory.resolve("verdict"), "pass", workflow, steps, new Request(PASS, false),
                new Report("accepted", PASS, List.of("PASS: Coverage present")));
        ExampleRunner.success(directory.resolve("verdict"), "fail", workflow, steps, new Request(FAIL, false),
                new Report("changes requested", FAIL, List.of("FAIL: Missing coverage")));
        ExampleRunner.success(directory.resolve("verdict"), "inconclusive", workflow, steps,
                new Request(INCONCLUSIVE, false), new Report("needs attention", INCONCLUSIVE,
                    List.of("ERROR: Scanner unavailable", "ABSTAIN: No evidence")));
        ExampleRunner.success(directory.resolve("verdict"), "excluded", workflow, steps,
                new Request(NOT_APPLICABLE, false), new Report("outside scope", NOT_APPLICABLE,
                    List.of("NOT_APPLICABLE: Documentation only")));
        // A thrown evaluation is an execution failure, not a fabricated INCONCLUSIVE value.
        ExampleRunner.failure(directory.resolve("verdict"), "thrown", workflow, steps,
                new Request(PASS, true), "ASSESSMENT_FAILED");
    }

    public static final class Assess implements Step<Request, Verdict> {
        public Verdict execute(StepContext context, Request input) {
            if (input.throwFailure()) throw new IllegalStateException("Evaluation failed");
            var first = switch (input.fixture()) {
                case PASS -> Judgment.pass("Coverage present");
                case FAIL -> Judgment.fail("Missing coverage");
                case INCONCLUSIVE -> Judgment.error("Scanner unavailable");
                case NOT_APPLICABLE -> Judgment.notApplicable("Documentation only");
            };
            var jury = SimpleJury.builder().parallel(false)
                    .seat(JudgeSeat.named("coverage", () -> first).notApplicableWhen("Documentation only"))
                    .votingStrategy(new AllEligiblePassStrategy(ErrorHandling.PROPAGATE, ExclusionHandling.EXCLUDE));
            if (input.fixture() == INCONCLUSIVE) jury.judge("security", () -> Judgment.abstain("No evidence"));
            return jury.build().vote();
        }
    }
    public static final class Explain implements Step<ReviewInput, Report> {
        private final String heading;
        public Explain(String heading) { this.heading = heading; }
        public Report execute(StepContext context, ReviewInput input) {
            var evidence = input.assessment().individual().stream()
                    .map(j -> j.status() + ": " + j.reasoning()).toList();
            return new Report(heading, input.conclusion(), evidence);
        }
    }
}
