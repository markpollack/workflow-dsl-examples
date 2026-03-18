package io.github.markpollack.workflow.tutorial.module08;

import io.github.markpollack.workflow.core.AgentContext;
import io.github.markpollack.workflow.flows.Step;
import io.github.markpollack.workflow.flows.steps.ChatClientStep;
import io.github.markpollack.workflow.flows.workflow.Workflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.concurrent.atomic.AtomicInteger;

public class SupervisorDemo {

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

        System.out.println("=== Module 08: Supervisor ===\n");

        // Track how many times each agent is called
        AtomicInteger reviewCount = new AtomicInteger(0);
        AtomicInteger editCount = new AtomicInteger(0);

        // Review agent: suggests improvements
        Step<String, String> review = Step.named("review", (ctx, input) -> {
            reviewCount.incrementAndGet();
            return ChatClientStep.of(chat,
                    "Review this text and suggest one specific improvement: {input}")
                    .execute(ctx, input);
        });

        // Edit agent: makes the text more concise
        Step<String, String> edit = Step.named("edit", (ctx, input) -> {
            editCount.incrementAndGet();
            return ChatClientStep.of(chat,
                    "Edit this text to be more concise while preserving meaning: {input}")
                    .execute(ctx, input);
        });

        // Supervisor: delegates to review/edit for 3 iterations
        @SuppressWarnings("unchecked")
        Object result = Workflow.<String, Object>supervisor("text-improver", chat)
                .agents(review, edit)
                .until(ctx -> ctx.get(AgentContext.ITERATION_COUNT).orElse(0) >= 3)
                .run("The very big and extremely large dragon was flying very high up " +
                     "in the sky above the tall mountains that were covered in snow.");

        System.out.println("Review agent called: " + reviewCount.get() + " times");
        System.out.println("Edit agent called:   " + editCount.get() + " times");
        System.out.println();
        System.out.println("Final result:\n" + result);

        System.out.println("\nDone.");
    }
}
