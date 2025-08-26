package chat.application;

import chat.domain.ChatModelPort;
import chat.domain.ChatService;
import chat.domain.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatUseCase implements ChatService {
    private final ChatModelPort modelPort;
    private final Map<String, List<Message>> history = new ConcurrentHashMap<>();
    private final Set<String> forbiddenWords = Set.of("maldición", "prohibido"); // ejemplo

    public ChatUseCase(ChatModelPort modelPort) {
        this.modelPort = modelPort;
    }

    @Override
    public String chat(List<Message> messages, String conversationId) {
        validate(messages);
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        history.put(conversationId, new ArrayList<>(messages));
        return modelPort.chat(messages);
    }

    @Override
    public void chatStream(List<Message> messages, String conversationId, Consumer<String> onChunk) throws Exception {
        validate(messages);
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        history.put(conversationId, new ArrayList<>(messages));
        modelPort.chatStream(messages, onChunk);
    }

    private void validate(List<Message> messages) {
        if (messages == null || messages.isEmpty()) throw new IllegalArgumentException("messages vacío");
        for (Message m : messages) {
            if (m.getContent() == null || m.getContent().length() < 1 || m.getContent().length() > 2000)
                throw new IllegalArgumentException("content inválido");
            for (String word : forbiddenWords) {
                if (m.getContent().toLowerCase().contains(word))
                    throw new IllegalArgumentException("Mensaje contiene palabra prohibida");
            }
        }
    }
}
