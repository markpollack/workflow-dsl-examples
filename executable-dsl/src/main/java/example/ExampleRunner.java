package example;

import java.nio.file.Path;
import java.util.Map;
import io.github.markpollack.workflow.batch.durable.DurableWorkflows;
import io.github.markpollack.workflow.batch.durable.ExecutionCompatibility;
import io.github.markpollack.workflow.batch.durable.ExecutionPolicy;
import io.github.markpollack.workflow.batch.durable.RunSnapshot;
import io.github.markpollack.workflow.batch.durable.StepRegistry;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.compiler.ValidatedWorkflow;

/** Application wiring and assertions shared by the six executable lessons. */
final class ExampleRunner {
    private ExampleRunner() {}

    static void success(Path store, String key, ValidatedWorkflow workflow,
            Map<String, Step<?, ?>> steps, Object input, Object expected) {
        try (var runtime = open(store, steps)) {
            var run = runtime.start(workflow, key, input);
            var end = runtime.resume(run.runId(), workflow);
            if (end.status() != RunSnapshot.Status.SUCCEEDED)
                throw new IllegalStateException("Expected success: " + end.reason());
            var actual = runtime.result(run.runId(), workflow);
            if (!expected.equals(actual)) throw new IllegalStateException("Unexpected result: " + actual);
            System.out.println(workflow.definition().name() + ": " + actual);
        }
    }

    static void failure(Path store, String key, ValidatedWorkflow workflow,
            Map<String, Step<?, ?>> steps, Object input, String reason) {
        try (var runtime = open(store, steps)) {
            var run = runtime.start(workflow, key, input);
            var end = runtime.resume(run.runId(), workflow);
            if (end.status() != RunSnapshot.Status.FAILED || !reason.equals(end.reason().code()))
                throw new IllegalStateException("Unexpected failure: " + end);
            System.out.println(workflow.definition().name() + ": " + reason);
        }
    }

    private static DurableWorkflows open(Path store, Map<String, Step<?, ?>> steps) {
        var registry = StepRegistry.of(steps);
        var deployment = new ExecutionCompatibility("dsl-tutorial", "dsl-tutorial-0.13.0-v1",
                Map.of("fixture", "deterministic"));
        // Physical Step concurrency is separate from each forEach's logical item bound.
        var policy = new ExecutionPolicy(3, 32, 10_000, 2);
        return DurableWorkflows.open(store, registry, deployment, policy);
    }
}
