package zm.unza.counseling.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zm.unza.counseling.entity.AiProviderConfig;
import zm.unza.counseling.entity.AiProviderType;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** LlmClient against a local stand-in for DeepSeek's OpenAI-compatible API. */
class LlmClientDeepSeekTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private final List<JsonNode> bodies = new CopyOnWriteArrayList<>();
    private final List<String> auths = new CopyOnWriteArrayList<>();
    private final AtomicInteger failFirst = new AtomicInteger();
    private volatile int failStatus = 503;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", ex -> {
            byte[] in = ex.getRequestBody().readAllBytes();
            bodies.add(mapper.readTree(in));
            auths.add(ex.getRequestHeaders().getFirst("Authorization"));
            String out; int code;
            if (failFirst.getAndDecrement() > 0) {
                code = failStatus;
                out = "{\"error\":{\"message\":\"Server overloaded\"}}";
            } else {
                code = 200;
                out = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Eish, that sounds hard.\"},\"finish_reason\":\"stop\"}]}";
            }
            byte[] b = out.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(code, b.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(b); }
        });
        server.createContext("/models", ex -> {
            byte[] b = "{\"data\":[{\"id\":\"deepseek-v4-pro\"},{\"id\":\"deepseek-flash\"}]}".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(b); }
        });
        server.start();
    }

    @AfterEach
    void stop() { server.stop(0); }

    private AiProviderConfig cfg() {
        AiProviderConfig c = new AiProviderConfig();
        c.setProviderType(AiProviderType.DEEPSEEK);
        c.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        c.setModel("deepseek-flash");
        c.setMaxTokens(1024);
        c.setTimeoutSeconds(10);
        return c;
    }

    @Test
    void sendsThinkingDisabledModelAndBearerKey() throws Exception {
        String reply = new LlmClient(mapper).complete(cfg(), "sk-test", "Be kind.", List.of(AiMessage.user("hi")), null);

        assertEquals("Eish, that sounds hard.", reply);
        JsonNode body = bodies.get(0);
        assertEquals("deepseek-flash", body.path("model").asText());
        assertEquals("disabled", body.path("thinking").path("type").asText(), "thinking must be explicitly off by default");
        assertEquals(1024, body.path("max_tokens").asInt());
        assertEquals("system", body.path("messages").path(0).path("role").asText());
        assertEquals("Bearer sk-test", auths.get(0));
    }

    @Test
    void thinkingCanBeEnabled() throws Exception {
        AiProviderConfig c = cfg();
        c.setThinkingEnabled(true);
        new LlmClient(mapper).complete(c, "sk-test", null, List.of(AiMessage.user("hi")), null);
        assertEquals("enabled", bodies.get(0).path("thinking").path("type").asText());
    }

    @Test
    void retriesOnceOnTransientOverload() throws Exception {
        failFirst.set(1);
        String reply = new LlmClient(mapper).complete(cfg(), "sk-test", null, List.of(AiMessage.user("hi")), null);
        assertEquals("Eish, that sounds hard.", reply);
        assertEquals(2, bodies.size(), "one failed attempt + one retry");
    }

    @Test
    void explainsBadKey() {
        failFirst.set(5);
        failStatus = 401;
        LlmClient.LlmException e = assertThrows(LlmClient.LlmException.class, () ->
                new LlmClient(mapper).complete(cfg(), "sk-wrong", null, List.of(AiMessage.user("hi")), null));
        assertTrue(e.getMessage().contains("check the API key"), e.getMessage());
        assertEquals(1, bodies.size(), "401 is not retried");
    }

    @Test
    void listsModels() throws Exception {
        assertEquals(List.of("deepseek-flash", "deepseek-v4-pro"), new LlmClient(mapper).listModels(cfg(), "sk-test"));
    }

    @Test
    void otherProvidersDoNotGetTheThinkingParameter() throws Exception {
        AiProviderConfig c = cfg();
        c.setProviderType(AiProviderType.GROQ);
        new LlmClient(mapper).complete(c, "gsk-test", null, List.of(AiMessage.user("hi")), null);
        assertTrue(bodies.get(0).path("thinking").isMissingNode());
    }
}
