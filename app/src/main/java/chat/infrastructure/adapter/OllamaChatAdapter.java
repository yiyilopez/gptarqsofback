package chat.infrastructure.adapter;

import chat.domain.ChatModelPort;
import chat.domain.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

public class OllamaChatAdapter implements ChatModelPort {
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaChatAdapter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String chat(List<Message> messages) {
        try {
            URL url = new URL(baseUrl + "/api/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            ObjectNode body = mapper.createObjectNode();
            body.put("model", "llama3.1:8b");
            body.put("stream", false);
            body.set("messages", mapper.valueToTree(messages));
            conn.getOutputStream().write(mapper.writeValueAsBytes(body));
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            // parse response, return only the answer text
            String response = sb.toString();
            // Ollama responde con JSON: { ... , "message": { "role": ..., "content": "..." }, ... }
            try {
                var json = mapper.readTree(response);
                if (json.has("message") && json.get("message").has("content")) {
                    return json.get("message").get("content").asText();
                }
            } catch (Exception ignored) {}
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void chatStream(List<Message> messages, Consumer<String> onChunk) throws Exception {
        URL url = new URL(baseUrl + "/api/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "llama3.1:8b");
        body.put("stream", true);
        body.set("messages", mapper.valueToTree(messages));
        conn.getOutputStream().write(mapper.writeValueAsBytes(body));
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            // Cada chunk es un JSON: { ..., "message": { "content": "..." }, ... }
            try {
                var json = mapper.readTree(line);
                if (json.has("message") && json.get("message").has("content")) {
                    String content = json.get("message").get("content").asText();
                    onChunk.accept(content);
                }
            } catch (Exception e) {
                // Si no es JSON, ignora el chunk
            }
        }
        reader.close();
    }
}
