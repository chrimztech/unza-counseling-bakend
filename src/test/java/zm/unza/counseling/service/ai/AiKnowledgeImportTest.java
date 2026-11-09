package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import zm.unza.counseling.entity.AiAssistantSettings;
import zm.unza.counseling.entity.AiCounsellingScenario;
import zm.unza.counseling.repository.AiAssistantSettingsRepository;
import zm.unza.counseling.repository.AiCategoryGuidanceRepository;
import zm.unza.counseling.repository.AiCounsellingScenarioRepository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Runs the real importer and retrieval against a real dialogue workbook. Point it at a file with
 * {@code -Dai.template=path/to/AI_Counselling_Dialogue_Template.xlsx}; skipped when unset.
 */
class AiKnowledgeImportTest {

    private static Path template() {
        String p = System.getProperty("ai.template");
        Assumptions.assumeTrue(p != null && Files.exists(Path.of(p)), "set -Dai.template to run");
        return Path.of(p);
    }

    @Test
    void importsTemplateAndRetrievesRelevantScenarios() throws Exception {
        AiScenarioImporter.ImportResult r;
        try (InputStream in = new FileInputStream(template().toFile())) {
            r = new AiScenarioImporter().parse(in, template().getFileName().toString());
        }

        System.out.println("Sheets: " + r.sheetsRead());
        System.out.println("Scenarios: " + r.scenarios().size() + ", duplicates skipped: " + r.duplicatesSkipped()
                + ", guidance: " + r.guidance().size() + ", warnings: " + r.warnings());
        long withResp = r.scenarios().stream().filter(s -> s.getIdealResponse() != null).count();
        long gold = r.scenarios().stream().filter(AiCounsellingScenario::getGoldExample).count();
        long drafts = r.scenarios().stream().filter(s -> Boolean.TRUE.equals(s.getAiDrafted())).count();
        long reviewNotes = r.scenarios().stream().filter(s -> s.getReviewNote() != null).count();
        System.out.println("With response: " + withResp + ", gold: " + gold + ", AI drafts: " + drafts + ", review notes: " + reviewNotes);
        // Drafts must never be mistaken for counsellor-written or curated examples.
        assertTrue(r.scenarios().stream().noneMatch(s -> Boolean.TRUE.equals(s.getAiDrafted()) && s.getGoldExample()));

        assertFalse(r.scenarios().isEmpty());
        assertTrue(withResp > 0);
        // Contributor names must never be imported into any field.
        for (AiCounsellingScenario s : r.scenarios()) {
            String all = String.join("|", String.valueOf(s.getStudentMessage()), String.valueOf(s.getIdealResponse()),
                    String.valueOf(s.getEscalation()), String.valueOf(s.getKeyTopics()), String.valueOf(s.getNotesForAi()));
            assertFalse(all.contains("Namakando"), "contributor name leaked: " + s.getScenarioCode());
            assertFalse(all.contains("Sichali"), "contributor name leaked: " + s.getScenarioCode());
        }

        // Retrieval over the parsed scenarios (repositories mocked; only responses are retrievable).
        r.scenarios().forEach(s -> s.setHasResponse(s.getIdealResponse() != null && !s.getIdealResponse().isBlank()));
        List<AiCounsellingScenario> pool = r.scenarios().stream().filter(AiCounsellingScenario::getHasResponse).toList();
        AiCounsellingScenarioRepository scen = mock(AiCounsellingScenarioRepository.class);
        when(scen.findByEnabledTrueAndHasResponseTrue()).thenReturn(pool);
        AiCategoryGuidanceRepository guid = mock(AiCategoryGuidanceRepository.class);
        when(guid.findAllByOrderBySortOrderAscIdAsc()).thenReturn(r.guidance());
        AiAssistantSettingsRepository set = mock(AiAssistantSettingsRepository.class);
        when(set.findById(AiAssistantSettings.SINGLETON_ID)).thenReturn(Optional.of(new AiAssistantSettings()));
        AiKnowledgeService svc = new AiKnowledgeService(scen, guid, set, new AiScenarioImporter());

        String[] probes = {
                "I can't pay my tuition fees and I might be deregistered",
                "my boyfriend cheated on me and I can't eat or sleep",
                "I failed two courses and my parents will be so disappointed",
                "I feel like everyone would be better off without me",
                "someone stole my accommodation money, I paid a man who promised me a room",
                "I have a gambling problem and lost my allowance betting",
        };
        for (String q : probes) {
            List<AiKnowledgeService.RetrievedExample> hits = svc.retrieve(q, 3);
            System.out.println("\nQ: " + q);
            hits.forEach(h -> System.out.printf("  %.2f [%s/%s] %s%n", h.score(), h.scenario().getProblemCategory(),
                    h.scenario().getRiskLevel(), abbreviate(h.scenario().getStudentMessage())));
            assertFalse(hits.isEmpty(), "no retrieval hit for: " + q);
        }

        String prompt = svc.buildWellnessSystemPrompt(probes[3], null).systemPrompt();
        System.out.println("\n--- prompt length: " + prompt.length() + " chars");
        assertTrue(prompt.contains("Examples written by UNZA counsellors"));
    }

    private static String abbreviate(String s) {
        return s.length() <= 110 ? s : s.substring(0, 110) + "…";
    }
}
