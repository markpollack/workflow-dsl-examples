# Executable DSL: six runnable lessons

Use Java 21 and the repository's `./mvnw` wrapper.
This parentless module uses **Agent Workflow 0.13.0** and **Agent Judge 0.18.0**.
Both resolve from Maven Central.
The progression uses only methods available in 0.13.0.

```bash
./mvnw -f executable-dsl/pom.xml compile exec:java -Dexec.args=/tmp/workflow-tutorial
```

Repeat with the same store directory to reuse committed results.
All Steps and judgments are deterministic fixtures; no provider calls or API keys.
The program asserts the expected typed reports, all four native conclusions, preserved ERROR/ABSTAIN details, and distinct thrown-evaluation failure.

| Read in order | Contracts and purpose |
| --- | --- |
| [Sequential.java](src/main/java/example/Sequential.java) | Request → Diff → ReportInput(Request, Diff): two Steps and an earlier-value capture. |
| [Nested.java](src/main/java/example/Nested.java) | Diff → child Assessment; parent ReportInput combines the child result with its request. |
| [EnumDecision.java](src/main/java/example/EnumDecision.java) | Concrete Route enum, exhaustive arms, branch result convergence. |
| [NativeVerdict.java](src/main/java/example/NativeVerdict.java) | Native Jury routing over PASS/FAIL/INCONCLUSIVE/NOT_APPLICABLE, with full evidence. |
| [ParallelAssessments.java](src/main/java/example/ParallelAssessments.java) | Quality and Backport roles combine in a typed report. |
| [FanOut.java](src/main/java/example/FanOut.java) | Runtime file discovery, per-item review and manifest-order aggregation. |
| [ExampleRunner.java](src/main/java/example/ExampleRunner.java) | Registration, compatibility and physical capacity, then start/resume/result. |

Each lesson declares its concrete records beside its fluent definition.
The next Step's input type states which values it needs; the compiler derives those bindings.
Constructors supply settings and services.
`StepContext` holds execution metadata, never business values.
The maps in the runner hold registration/compatibility metadata only.
`end()` closes a lexical block; `terminate(...)` closes an execution path.

## Bounds and recovery

`maxItems` refuses oversized input before item effects; no truncation.
The saved manifest gives every occurrence a distinct stable index, including equal values.
`maxInFlight` limits admitted unsettled whole item workflows, separately from the runner's two physical Step slots.
An item waiting for descendants keeps its logical occupancy; coordinators do not use worker slots.
Results remain in input order; empty input produces an empty list.
`allSuccessful` prevents successful joins with missing results; a negative assessment remains a returned value.

Recovery reuses committed results and accepted routes/manifests with the compatible application deployment.
Unresolved effects can repeat within saved attempt/deadline allowances.
There is no exactly-once external-effect guarantee.
Store format 9 refuses earlier formats without migration; binary rollback does not downgrade stores.
Terminal failed/cancelled runs cannot be reopened.
Loops, waits/timers and Workflow committed-event Journal projection are unavailable in this milestone.
The canonical [tutorial](https://lab.pollack.ai/docs/agent-workflow/tutorial) explains the progression.

## Unreleased: `Sequence.build()`

A later, unreleased version adds `Sequence.build()` for a parent ending in an always-failing/cancelling child.
It is **not part of 0.13.0** and is intentionally absent from this module.
Ordinary paths still require explicit terminal intent.
No subsequent version is selected.
