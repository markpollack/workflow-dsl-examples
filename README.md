# Workflow DSL Examples

> **Documentation**: https://lab.pollack.ai/docs/agent-workflow/tutorial | [API Reference](https://lab.pollack.ai/docs/agent-workflow/api-reference)

Progressive, hands-on examples for [Agent Workflow](https://github.com/markpollack/agent-workflow) — every module makes real LLM calls (GPT-4.1, temperature 0.3).

## Prerequisites

- Java 21+
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- An OpenAI API key (`OPENAI_API_KEY` environment variable)

## Getting Started

### Step 1: Build All Modules

```bash
git clone https://github.com/markpollack/workflow-dsl-examples.git
cd workflow-dsl-examples
./mvnw compile
```

### Step 2: Run Any Module

```bash
export OPENAI_API_KEY=sk-...

# Module 01: Sequential pipeline
./mvnw exec:java -pl module-01-sequential

# Module 07: Quality gate
./mvnw exec:java -pl module-07-gate
```

## Tutorial Structure

### DSL Primitives

| Module | Title | What You'll Learn |
|--------|-------|-------------------|
| 01 | Sequential Pipeline | Chain steps with `.step().then().then()` |
| 02 | Branch | Route on a predicate with `.branch().then().otherwise()` |
| 03 | Error Recovery | Handle exceptions with `.onError(ex, recovery)` |
| 04 | Loop | Iterate until quality converges with `repeatUntilOutput()` |
| 05 | Parallel | Fan-out concurrent steps with `.parallel()` |
| 06 | Decision | LLM-driven routing with `.decision().option().option()` |
| 07 | Gate | Quality checkpoint with `.gate().onPass().onFail()` |
| 08 | Supervisor | Autonomous agent delegation with `.supervisor().agents().until()` |

## Integration Testing

The tutorial includes an automated test suite with two-gate validation:
1. **Deterministic gate** — required output strings must appear
2. **AI gate** — Claude Haiku validates semantic correctness

```bash
cd integration-testing

# Run all tests
./scripts/run-integration-tests.sh

# Run a quick subset (modules 01-03)
./scripts/run-integration-tests.sh --quick

# Run a single module test
jbang RunIntegrationTest.java module-01-sequential
```

## Versions

| Dependency | Version |
|---|---|
| Agent Workflow | 0.6.0 |
| Spring AI | 2.0.0-M3 |

## Related Projects

- [Agent Workflow](https://github.com/markpollack/agent-workflow) — The workflow library this tutorial teaches
- [Agent Workflow Documentation](https://lab.pollack.ai/docs/agent-workflow/getting-started) — Full docs with getting started guide
- [Agent Judge](https://github.com/markpollack/agent-judge) — Evaluation framework used in gate examples

## License

Apache 2.0
