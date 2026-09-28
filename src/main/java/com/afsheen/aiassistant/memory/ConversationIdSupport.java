package com.afsheen.aiassistant.memory;

import java.util.UUID;

/**
 * Spring AI's {@code ChatMemoryRepository} contract only carries a
 * conversationId - it has no concept of "which student owns this session".
 * Rather than bolt on a second lookup table, every conversationId this
 * assistant issues is minted as {@code <studentId>::<uuid>}, so ownership can
 * be recovered (and enforced) from the id itself wherever Spring AI only
 * gives us the id back - e.g. inside {@link JpaChatMemoryRepository}.
 */
public final class ConversationIdSupport {

    private static final String DELIMITER = "::";

    private ConversationIdSupport() {
    }

    public static String newConversationId(String studentId) {
        return studentId + DELIMITER + UUID.randomUUID();
    }

    public static String studentIdOf(String conversationId) {
        if (conversationId == null) {
            return null;
        }
        int idx = conversationId.indexOf(DELIMITER);
        return idx > 0 ? conversationId.substring(0, idx) : null;
    }

    public static boolean belongsTo(String conversationId, String studentId) {
        return studentId != null && studentId.equals(studentIdOf(conversationId));
    }
}
