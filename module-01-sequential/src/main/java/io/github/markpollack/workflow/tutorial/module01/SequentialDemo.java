package io.github.markpollack.workflow.tutorial.module01;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class SequentialDemo {

    public static void main(String[] args) {
        System.out.println("=== Module 01: Sequential Pipeline ===\n");

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
        Step<String, String> write = ChatClientStep.of(chat,
                "Write a short 3-sentence story about: {input}");

        Step<String, String> editForAudience = ChatClientStep.of(chat,
                "Rewrite the following story so it is appropriate for children aged 5-8. "
                + "Keep it fun and simple:\n\n{input}");

        Step<String, String> editForStyle = ChatClientStep.of(chat,
                "Polish the following story for vivid imagery and rhythmic prose. "
                + "Keep it under 5 sentences:\n\n{input}");

        // -- Workflow --
        String result = Workflow.<String, String>define("sequential-pipeline")
                .step(write)
                .then(editForAudience)
                .then(editForStyle)
                .run("dragons and wizards");

        System.out.println("Final story:\n" + result);
        System.out.println("\nDone.");
    }
}
