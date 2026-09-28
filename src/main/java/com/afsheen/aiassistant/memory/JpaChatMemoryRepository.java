package com.afsheen.aiassistant.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.afsheen.aiassistant.entity.AssistantChatMessage;
import com.afsheen.aiassistant.repository.AssistantChatMessageRepository;

/**
 * Backs Spring AI's {@link ChatMemory} with our own Liquibase-managed table
 * instead of the spring-ai-provided JDBC chat-memory starter, which manages
 * its own schema outside Liquibase. This keeps every table in this project
 * under a single migration tool.
 *
 * {@link org.springframework.ai.chat.memory.MessageWindowChatMemory} calls
 * {@link #saveAll} with the *entire* trimmed conversation window on every
 * turn (not just the new message), so this implementation replaces the
 * stored rows for that conversation rather than appending to them.
 */
@Component
public class JpaChatMemoryRepository implements ChatMemoryRepository {

    private final AssistantChatMessageRepository repository;

    public JpaChatMemoryRepository(AssistantChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> findConversationIds() {
        return repository.findAll().stream()
                .map(AssistantChatMessage::getConversationId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return repository.findByConversationIdOrderBySequenceNoAsc(conversationId).stream()
                .map(JpaChatMemoryRepository::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveAll(String conversationId, List<Message> messages) {
        repository.deleteByConversationId(conversationId);

        String studentId = ConversationIdSupport.studentIdOf(conversationId);
        List<AssistantChatMessage> rows = new ArrayList<>();
        int sequence = 0;
        for (Message message : messages) {
            AssistantChatMessage row = new AssistantChatMessage();
            row.setConversationId(conversationId);
            row.setStudentId(studentId);
            row.setMessageType(message.getMessageType());
            row.setContent(message.getText());
            row.setSequenceNo(sequence++);
            rows.add(row);
        }
        repository.saveAll(rows);
    }

    @Override
    @Transactional
    public void deleteByConversationId(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

    /**
     * Reconstructs a plain text {@link Message} of the right type. Structured
     * tool-call payloads (e.g. an assistant's function-call arguments) are not
     * round-tripped - only their rendered text is - which is sufficient
     * context for the model on the next turn, even though it loses the exact
     * tool-call metadata of that turn.
     */
    private static Message toSpringAiMessage(AssistantChatMessage row) {
        MessageType type = row.getMessageType();
        String text = row.getContent();
        if (type == MessageType.ASSISTANT) {
            return new AssistantMessage(text);
        }
        if (type == MessageType.SYSTEM) {
            return new SystemMessage(text);
        }
        if (type == MessageType.TOOL) {
            // ToolResponseMessage has no public text-only constructor, and
            // (per the default advisor ordering since Spring AI 1.1) tool
            // messages generally aren't written to ChatMemoryRepository
            // anyway. Represented as plain text so history still reads
            // sensibly if one ever is.
            return new AssistantMessage("[tool result] " + text);
        }
        return new UserMessage(text);
    }
}
