package chat.domain;

import java.util.List;

public interface ChatService {
    String chat(List<Message> messages, String conversationId);
    void chatStream(List<Message> messages, String conversationId, java.util.function.Consumer<String> onChunk) throws Exception;
}
