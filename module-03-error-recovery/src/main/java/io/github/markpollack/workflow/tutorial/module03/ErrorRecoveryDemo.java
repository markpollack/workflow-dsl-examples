package io.github.markpollack.workflow.tutorial.module03;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class ErrorRecoveryDemo {

    public static void main(String[] args) {
        System.out.println("=== Module 03: Error Recovery ===\n");

        // -- ChatClient setup --
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

        // -- Steps --
        Step<String, String> riskyStep = Step.named("risky-step", (ctx, input) -> {
            System.out.println("  [risky-step] received: " + input);
            if (input.toLowerCase().contains("bad")) {
                throw new IllegalArgumentException("Input contained 'bad' — refusing to process");
            }
            return "Processed: " + input;
        });

        Step<String, String> recovery = Step.named("recovery", (ctx, input) -> {
            System.out.println("  [recovery] handling error, generating fallback via LLM...");
            return chat.prompt()
                    .user("The previous step failed. Generate a safe fallback response for: " + input)
                    .call().content();
        });

        Step<String, String> finalStep = Step.named("final-step", (ctx, input) -> {
            System.out.println("  [final-step] received: " + input);
            return "Final: " + input;
        });

        // -- Workflow --
        String result = Workflow.<String, String>define("error-recovery")
                .step(riskyStep)
                    .onError(IllegalArgumentException.class, recovery)
                .then(finalStep)
                .run("bad input");

        System.out.println("\nResult:\n" + result);
        System.out.println("\nDone.");
    }
}
