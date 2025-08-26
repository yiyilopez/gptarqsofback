package chat.domain;

import java.util.List;

public interface ChatModelPort {
    String chat(List<Message> messages);
    void chatStream(List<Message> messages, java.util.function.Consumer<String> onChunk) throws Exception;
}
