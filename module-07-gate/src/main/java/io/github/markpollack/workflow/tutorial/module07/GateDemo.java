package io.github.markpollack.workflow.tutorial.module07;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Gate;
import io.github.markpollack.workflow.flows.workflow.GateDecision;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.regex.Pattern;

public class GateDemo {

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY must be set");
            System.exit(1);
        }
        OpenAiApi api = OpenAiApi.builder().apiKey(apiKey).build();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4.1")
                        .maxTokens(1024)
                        .temperature(0.3)
                        .build())
                .build();
        ChatClient chat = ChatClient.builder(model).build();

        System.out.println("=== Module 07: Gate ===\n");

        // Step 1: generate a short story
        Step<String, String> generate = ChatClientStep.of(chat,
                "Write a very short story (3-4 sentences) about {input}.");

        // Quality gate: LLM scores the story, >= 0.7 passes
        Pattern SCORE_PATTERN = Pattern.compile("(\\d+\\.\\d+|\\d+)");

        Gate<Object> qualityGate = (ctx, output) -> {
            String scoreResponse = chat.prompt()
                    .user("Rate the quality of this story from 0.0 to 1.0. " +
                          "Reply with ONLY a decimal number, nothing else.\n\n" + output)
                    .call().content();

            double score;
            try {
                score = Double.parseDouble(scoreResponse.trim());
            } catch (NumberFormatException e) {
                // Regex fallback
                var matcher = SCORE_PATTERN.matcher(scoreResponse);
                score = matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
            }
            System.out.println("Gate score: " + score);
            return score >= 0.7 ? GateDecision.PASS : GateDecision.FAIL;
        };

        // Pass/fail steps
        Step<Object, String> approve = Step.named("approve",
                (ctx, input) -> "APPROVED: " + input);

        Step<Object, String> reject = Step.named("reject",
                (ctx, input) -> "REJECTED: " + input);

        // Wire the workflow
        String result = Workflow.<String, String>define("quality-check")
                .step(generate)
                .gate(qualityGate)
                    .onPass(approve)
                    .onFail(reject)
                .end()
                .run("a heroic knight");

        System.out.println();
        if (result.startsWith("APPROVED")) {
            System.out.println("Route taken: APPROVED");
        } else {
            System.out.println("Route taken: REJECTED");
        }
        System.out.println(result);

        System.out.println("\nDone.");
    }
}
