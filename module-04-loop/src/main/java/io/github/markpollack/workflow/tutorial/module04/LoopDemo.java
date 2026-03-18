package io.github.markpollack.workflow.tutorial.module04;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoopDemo {

    public static void main(String[] args) {
        System.out.println("=== Module 04: Loop ===\n");

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
        AtomicInteger iteration = new AtomicInteger(0);
        Pattern scorePattern = Pattern.compile("(\\d+\\.\\d+)");

        Step<Object, String> editor = Step.named("editor", (ctx, input) -> {
            int iter = iteration.incrementAndGet();
            System.out.println("  [editor] iteration " + iter);
            String text = (input instanceof Double)
                    ? "Previous humor score was " + input + ". Make it funnier."
                    : String.valueOf(input);
            return chat.prompt()
                    .user("You are a comedy writer. Take this text and make it funnier. "
                          + "Add wordplay, absurd details, or unexpected twists:\n\n" + text)
                    .call().content();
        });

        Step<Object, Double> scorer = Step.named("scorer", (ctx, input) -> {
            String response = chat.prompt()
                    .user("Rate the humor of the following text on a scale from 0.0 to 1.0. "
                          + "Respond with ONLY a decimal number, nothing else:\n\n" + input)
                    .call().content();

            double score;
            try {
                score = Double.parseDouble(response.trim());
            } catch (NumberFormatException e) {
                Matcher m = scorePattern.matcher(response);
                score = m.find() ? Double.parseDouble(m.group(1)) : 0.0;
            }
            System.out.println("  [scorer] score = " + score);
            return score;
        });

        // -- Workflow --
        Object result = Workflow.define("humor-loop")
                .repeatUntilOutput(score -> score instanceof Double d && d >= 0.6)
                    .step(editor)
                    .step(scorer)
                .end()
                .run("Why did the chicken cross the road? To get to the other side.");

        System.out.println("\nFinal result: " + result);
        System.out.println("\nDone.");
    }
}
