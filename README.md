# Workflow DSL Examples

Start with the [executable DSL progression](executable-dsl/README.md): six deterministic Java 21 examples for the prepared Agent Workflow 0.13.0 milestone.
No provider, API key or Spring container is required.

[Canonical tutorial](https://lab.pollack.ai/docs/agent-workflow/tutorial) · [API reference](https://lab.pollack.ai/docs/agent-workflow/api-reference)

## Run the current progression

```bash
./mvnw -f executable-dsl/pom.xml compile exec:java -Dexec.args=/tmp/workflow-tutorial
```

Workflow 0.13.0 is prepared for publication; until Maven Central publication is confirmed, use staged artifacts with `-Dmaven.repo.local=/path/to/staged-repository`.
Judge 0.18.0 is published.
Run again with the same directory to reuse committed results.

## Historical provider comparisons

The root reactor's eight `module-*` examples and `integration-testing` tools target the older Workflow DSL, with `workflow.version=0.10.0` in the root POM.
They teach legacy `Workflow.define`, context keys, loops, gates and supervisor APIs; these are not the 0.13.0 durable authoring path.
They remain as historical provider comparison examples, with their original dependencies and behavior.
Their execution requires a provider and can incur cost.
The standalone `executable-dsl` command does not build or execute them.

## License

[Apache 2.0](LICENSE)
