package io.github.markpollack.workflow.tutorial.module02;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class BranchDemo {

    public static void main(String[] args) {
        System.out.println("=== Module 02: Branch ===\n");

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
        Step<String, String> classify = ChatClientStep.of(chat,
                "Classify the following question as either 'medical' or 'legal'. "
                + "Respond with exactly one word, lowercase:\n\n{input}");

        Step<String, String> medicalExpert = ChatClientStep.of(chat,
                "You are a medical expert. Provide helpful medical guidance for: {input}");

        Step<String, String> legalExpert = ChatClientStep.of(chat,
                "You are a legal expert. Provide helpful legal guidance for: {input}");

        // -- Workflow --
        String result = Workflow.<String, String>define("branch-demo")
                .step(classify)
                .branch(output -> "medical".equals(((String) output).trim().toLowerCase()))
                    .then(medicalExpert)
                    .otherwise(legalExpert)
                .run("I broke my leg, what should I do?");

        System.out.println("Result:\n" + result);
        System.out.println("\nDone.");
    }
}
