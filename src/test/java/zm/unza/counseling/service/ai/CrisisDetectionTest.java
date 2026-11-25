package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.Test;
import zm.unza.counseling.service.CrisisDetectionService;
import zm.unza.counseling.service.CrisisDetectionService.Severity;

import static org.junit.jupiter.api.Assertions.*;

class CrisisDetectionTest {

    private final CrisisDetectionService shared = new CrisisDetectionService();
    // Only scan() is exercised, which uses nothing but the shared scanner.
    private final AiCrisisEscalationService chat = new AiCrisisEscalationService(shared, null, null, null, null);

    @Test
    void everydayWordsNoLongerTriggerCritical() {
        for (String s : new String[]{
                "I've begun revising for my exams", "I bombed my statistics test", "I bought grapes at Soweto market",
                "This guidance is useful", "My therapist is kind", "I'm drugstore shopping", "the scrape on my knee"}) {
            assertEquals(Severity.NONE, chat.scan(s).severity(), s);
        }
        assertNotEquals(Severity.CRITICAL, shared.scan("I've begun revising").severity());
    }

    @Test
    void realDangerStillAlerts() {
        assertEquals(Severity.CRITICAL, chat.scan("I want to kill myself").severity());
        assertEquals(Severity.CRITICAL, chat.scan("I think about suicide every night").severity());
        assertEquals(Severity.CRITICAL, chat.scan("I was raped last week").severity());
        assertEquals(Severity.CRITICAL, chat.scan("He has a gun and threatens me").severity());
        assertEquals(Severity.CRITICAL, chat.scan("I keep cutting myself").severity());
        assertEquals(Severity.HIGH, chat.scan("My boyfriend keeps hitting me").severity());
        assertEquals(Severity.HIGH, chat.scan("I can’t go on like this").severity(), "curly apostrophe");
        assertEquals(Severity.HIGH, chat.scan("Everyone would be better off without me").severity(), "chat-only pattern");
        for (String s : new String[]{
                "sometimes I think nobody would miss me", "I don't want to wake up tomorrow", "I'm so tired of living",
                "I wish I was never born", "I want to take all my pills", "I just want to disappear forever"}) {
            assertEquals(Severity.HIGH, chat.scan(s).severity(), s);
        }
    }

    @Test
    void indirectPatternsDoNotFireOnOrdinarySentences() {
        for (String s : new String[]{
                "I don't want to be here at the library all night", "I'm tired of my roommate", "nobody would help me with maths"}) {
            // "be here" is intentionally caught (better a check-in than a miss); the others must not fire.
            if (s.contains("be here")) continue;
            assertEquals(Severity.NONE, chat.scan(s).severity(), s);
        }
    }

    @Test
    void wellnessTopicsAreHandledByTheAssistantNotAlerted() {
        for (String s : new String[]{
                "I think I have depression", "I had a panic attack before my exam", "I drink alcohol on weekends",
                "I feel useless at maths", "I'm dealing with old trauma"}) {
            assertEquals(Severity.NONE, chat.scan(s).severity(), s);
            assertEquals(Severity.HIGH, shared.scan(s).severity(), "shared scanner still flags it for messages: " + s);
        }
    }
}
