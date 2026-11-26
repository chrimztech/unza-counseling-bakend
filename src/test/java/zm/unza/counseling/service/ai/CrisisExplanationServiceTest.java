package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.Test;
import zm.unza.counseling.service.CrisisDetectionService;
import zm.unza.counseling.service.ai.CrisisExplanationService.Source;
import zm.unza.counseling.service.ai.CrisisExplanationService.Span;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrisisExplanationServiceTest {

    private final CrisisDetectionService detector = new CrisisDetectionService();
    private final AiCrisisEscalationService chatScanner =
            new AiCrisisEscalationService(detector, null, null, null, null);
    private final CrisisExplanationService explainer = new CrisisExplanationService(detector, chatScanner);

    @Test
    void indirectWordingIsHighlightedAndDecisive() {
        String text = "Honestly everyone would be better off without me.";
        var ex = explainer.explain(text, Source.AI_CHAT);
        assertThat(ex.rating()).isEqualTo("HIGH");
        assertThat(ex.spans()).hasSize(1);
        Span s = ex.spans().get(0);
        assertThat(text.substring(s.start(), s.end())).isEqualToIgnoringCase("better off without me");
        assertThat(s.category()).isEqualTo("indirect suicidal thinking");
        assertThat(s.role()).isEqualTo("decisive");
        assertThat(s.ratingIfRemoved()).isEqualTo("NONE");
        assertThat(ex.whyFlagged()).contains("'better off without me' (indirect suicidal thinking)");
    }

    @Test
    void contextWordsInChatAreShownButNotTriggers() {
        // "hopeless" is a HIGH phrase that does not alert in the AI chat on its own.
        var ex = explainer.explain("I feel hopeless and I want to kill myself", Source.AI_CHAT);
        assertThat(ex.rating()).isEqualTo("CRITICAL");
        Span hopeless = ex.spans().stream().filter(s -> s.text().equals("hopeless")).findFirst().orElseThrow();
        assertThat(hopeless.role()).isEqualTo("context");
        // "i want to kill" and "kill myself" overlap: one span, labelled as self-directed.
        Span kill = ex.spans().stream().filter(s -> s.text().equalsIgnoreCase("I want to kill myself")).findFirst().orElseThrow();
        assertThat(kill.category()).isEqualTo("suicidal thinking");
        assertThat(kill.role()).isEqualTo("decisive");
        assertThat(ex.categories()).containsExactly("suicidal thinking");
    }

    @Test
    void twoIndependentTriggersAreSupportingNotDecisive() {
        var ex = explainer.explain("I am suicidal. I want to die.", Source.AI_CHAT);
        assertThat(ex.rating()).isEqualTo("CRITICAL");
        List<String> roles = ex.spans().stream().map(Span::role).distinct().toList();
        assertThat(roles).containsExactly("supporting");
        assertThat(ex.whyFlagged()).contains("No single phrase decided it");
    }

    @Test
    void phraseListCategoryBeatsGenericPatternLabel() {
        var ex = explainer.explain("I can't go on like this anymore", Source.AI_CHAT);
        assertThat(ex.rating()).isEqualTo("HIGH");
        assertThat(ex.spans()).hasSize(1);
        assertThat(ex.spans().get(0).category()).isEqualTo("hopelessness");
    }

    @Test
    void ordinaryTextIsNotFlagged() {
        var ex = explainer.explain("I bombed my exam and I have begun revising", Source.AI_CHAT);
        assertThat(ex.rating()).isEqualTo("NONE");
        assertThat(ex.spans()).isEmpty();
    }

    @Test
    void missingTextFallsBackToKeywords() {
        var ex = explainer.fromKeywords(List.of("overdose"), "CRITICAL");
        assertThat(ex.textAvailable()).isFalse();
        assertThat(ex.categories()).containsExactly("suicidal thinking");
        assertThat(ex.whyFlagged()).contains("'overdose'");
    }

    @Test
    void percentileIsNearestRank() {
        assertThat(AiOversightService.percentile(List.of(100L, 200L, 300L, 400L), 50)).isEqualTo(200L);
        assertThat(AiOversightService.percentile(List.of(100L, 200L, 300L, 400L), 95)).isEqualTo(400L);
        assertThat(AiOversightService.percentile(List.of(), 50)).isNull();
    }
}
