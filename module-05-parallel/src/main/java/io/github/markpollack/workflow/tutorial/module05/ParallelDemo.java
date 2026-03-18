package io.github.markpollack.workflow.tutorial.module05;

import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

public class ParallelDemo {

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

        System.out.println("=== Module 05: Parallel ===\n");

        // Two parallel steps: meals and movies
        Step<String, String> findMeals = ChatClientStep.of(chat,
                "Suggest 3 meals for a {input} evening. Just list the meal names, one per line.");

        Step<String, String> findMovies = ChatClientStep.of(chat,
                "Suggest 3 movies for a {input} evening. Just list the movie titles, one per line.");

        // Fan-out: both steps run concurrently on the same input
        @SuppressWarnings("unchecked")
        List<Object> results = (List<Object>) Workflow.define("evening-planner")
                .parallel(findMeals, findMovies)
                .run("romantic");

        System.out.println("Meals:\n" + results.get(0));
        System.out.println();
        System.out.println("Movies:\n" + results.get(1));

        System.out.println("\nDone.");
    }
}
