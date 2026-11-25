package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseUrlNormaliseTest {

    @Test
    void trimsEndpointPathsPastedFromSampleCode() {
        // The exact value that broke DeepSeek on 2026-11-25 (copied from a Python config).
        assertEquals("https://api.deepseek.com/v1",
                AiProviderAdminService.normaliseBaseUrl("https://api.deepseek.com/v1/chat/completions"));
        assertEquals("https://api.anthropic.com/v1", AiProviderAdminService.normaliseBaseUrl("https://api.anthropic.com/v1/messages/"));
        assertEquals("https://api.groq.com/openai/v1", AiProviderAdminService.normaliseBaseUrl(" https://api.groq.com/openai/v1/models "));
        assertEquals("https://api.openai.com/v1", AiProviderAdminService.normaliseBaseUrl("https://api.openai.com/v1/"));
        assertNull(AiProviderAdminService.normaliseBaseUrl("   "));
        assertNull(AiProviderAdminService.normaliseBaseUrl(null));
    }
}
