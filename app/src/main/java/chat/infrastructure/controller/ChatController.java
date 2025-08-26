package chat.infrastructure.controller;

import chat.application.ChatUseCase;
import chat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    private final ChatUseCase chatUseCase;

    @Autowired
    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String chat(@RequestBody ChatRequest req) {
        return chatUseCase.chat(req.getMessages(), req.getConversationId());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("cid") String conversationId, @RequestParam List<Message> messages) {
        SseEmitter emitter = new SseEmitter(60000L);
        new Thread(() -> {
            try {
                chatUseCase.chatStream(messages, conversationId, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    public static class ChatRequest {
        private List<Message> messages;
        private String conversationId;
        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    }
}
