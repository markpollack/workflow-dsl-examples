package io.github.markpollack.workflow.tutorial.module06;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class DecisionDemo {

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

        System.out.println("=== Module 06: Decision ===\n");

        // Two option steps
        Step<String, String> summarize = ChatClientStep.of(chat,
                "Summarize the following text in one sentence: {input}");

        Step<String, String> translate = ChatClientStep.of(chat,
                "Translate the following text to French: {input}");

        // LLM decides which option to pick
        String result = Workflow.<String, String>define("smart-router")
                .decision(chat)
                    .option("summarize", summarize)
                    .option("translate", translate)
                .end()
                .run("The quick brown fox jumps over the lazy dog. " +
                     "This classic pangram contains every letter of the English alphabet " +
                     "and has been used for typing practice since the late 1800s.");

        System.out.println("Decision result:\n" + result);

        System.out.println("\nDone.");
    }
}
