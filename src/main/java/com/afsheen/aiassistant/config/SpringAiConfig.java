package com.afsheen.aiassistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.afsheen.aiassistant.memory.JpaChatMemoryRepository;
import com.afsheen.aiassistant.prompt.AssistantSystemPrompt;
import com.afsheen.aiassistant.tools.AssistantTools;

@Configuration
public class SpringAiConfig {

    // Chat memory keeps the last N messages of a session in the prompt so
    // follow-up questions ("what about the second one?") resolve correctly.
    // A window (not the full unbounded history) keeps token usage/cost
    // bounded as a conversation grows; older turns roll off.
    private static final int CHAT_MEMORY_WINDOW_SIZE = 20;

    /**
     * Spring AI's ChatMemory abstraction, backed by our own Liquibase-managed
     * table (see JpaChatMemoryRepository) instead of an in-memory map, so
     * conversation context survives app restarts and can be queried back out
     * for the GET /history endpoint.
     */
    @Bean
    public ChatMemory chatMemory(JpaChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(CHAT_MEMORY_WINDOW_SIZE)
                .build();
    }

    @Bean
    public ChatClient studentAssistantChatClient(
            // Chat completions run on Gemini. Anthropic
            // (@Qualifier("anthropicChatModel")) and Mistral chat
            // (@Qualifier("mistralAiChatModel")) are wired up in
            // application.yml/pom.xml as alternates but not selected here -
            // switch this qualifier to move providers.
            @Qualifier("googleGenAiChatModel") ChatModel chatModel,
            AssistantTools assistantTools,
            ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AssistantSystemPrompt.BASE)
                .defaultTools(assistantTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * claude-sonnet-5 rejects temperature outright (HTTP 400 "`temperature`
     * is deprecated for this model"), and Spring AI hard-codes a 0.8
     * temperature into AnthropicChatProperties that no property value can
     * unset - an absent or empty spring.ai.anthropic.chat.options.temperature
     * just leaves the default in place, and a null on per-request options
     * loses to it during the merge. So clear it on the properties bean
     * itself, before AnthropicChatModel reads it, and the parameter never
     * goes on the wire. Static so the post-processor does not force this
     * configuration class to be instantiated early. Only relevant if you
     * switch the @Qualifier above back to Anthropic.
     */
    @Bean
    static BeanPostProcessor anthropicTemperatureDefaultRemover() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof AnthropicChatProperties properties) {
                    properties.getOptions().setTemperature(null);
                }
                return bean;
            }
        };
    }
}
