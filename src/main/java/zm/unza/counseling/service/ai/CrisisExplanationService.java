package zm.unza.counseling.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.service.CrisisDetectionService;
import zm.unza.counseling.service.CrisisDetectionService.Severity;

import java.util.*;
import java.util.function.Function;

/**
 * Word-level explanation of why the crisis detector flagged a piece of text, for counsellors.
 *
 * <p>The live detector is rule-based (whole-word phrase lists plus wording patterns), so there are no
 * model weights to attribute. Instead each matched phrase is given its position, a plain-English
 * category, and an <b>occlusion test</b>: the phrase is removed, the detector is run again, and the
 * change in rating is recorded. A phrase whose removal lowers the rating is <i>decisive</i>; one whose
 * removal changes nothing is <i>supporting</i> (other phrases would still have triggered the flag);
 * a phrase that was noticed but does not raise an alert on its own is <i>context</i>.</p>
 */
@Service
@RequiredArgsConstructor
public class CrisisExplanationService {

    public enum Source { AI_CHAT, MESSAGE, APPOINTMENT }

    /** One highlighted span of the text. */
    public record Span(int start, int end, String text, String category, String role,
                       String ratingIfRemoved, String effect) {}

    public record Explanation(String text, String rating, List<Span> spans, List<String> categories,
                              String whyFlagged, boolean textAvailable) {}

    private final CrisisDetectionService detector;
    private final AiCrisisEscalationService chatScanner;

    /** Most specific first; used when overlapping matches are merged. */
    private static final List<String> CATEGORY_PRIORITY = List.of(
            "suicidal thinking", "self-harm", "indirect suicidal thinking",
            "sexual violence", "risk of harm to others", "abuse or violence", "hopelessness",
            "substance use", "emotional distress",
            // Generic wording-pattern label: only used when no phrase-list match names it better.
            "suicidal thinking or self-harm", "crisis language");

    private static final Map<String, String> CATEGORY = new LinkedHashMap<>();
    static {
        for (String p : List.of("kill myself", "end my life", "take my own life", "want to die", "commit suicide",
                "suicidal", "suicide", "no reason to live", "life is not worth", "better off dead", "hang myself",
                "drown myself", "poison myself", "overdose", "slit my wrist", "slit wrists"))
            CATEGORY.put(p, "suicidal thinking");
        for (String p : List.of("self harm", "self-harm", "selfharm", "cutting myself", "hurting myself",
                "hurt myself", "harming myself"))
            CATEGORY.put(p, "self-harm");
        for (String p : List.of("planning to kill", "going to kill", "i want to kill", "kill him", "kill her",
                "kill them", "kill you", "harm someone", "hurt someone", "attack someone", "bomb", "bombs",
                "weapon", "weapons", "gun", "guns", "knife to"))
            CATEGORY.put(p, "risk of harm to others");
        for (String p : List.of("rape", "raped", "sexual assault"))
            CATEGORY.put(p, "sexual violence");
        for (String p : List.of("abuse", "abusing me", "being abused", "domestic violence", "beaten", "hitting me"))
            CATEGORY.put(p, "abuse or violence");
        for (String p : List.of("hopeless", "helpless", "worthless", "useless", "no point", "can't go on",
                "cannot go on", "can not go on", "give up on life", "end it all"))
            CATEGORY.put(p, "hopelessness");
        for (String p : List.of("drug", "drugs", "alcohol", "substance"))
            CATEGORY.put(p, "substance use");
        for (String p : List.of("depression", "anxiety attack", "panic attack", "breakdown", "trauma", "ptsd"))
            CATEGORY.put(p, "emotional distress");
    }

    public Explanation explain(String text, Source source) {
        if (text == null || text.isBlank()) {
            return new Explanation(null, "NONE", List.of(), List.of(), "The original text is no longer available.", false);
        }
        Function<String, Severity> rate = scannerFor(source);
        Severity rating = rate.apply(text);

        // Candidate spans: phrase-list hits and (for the AI chat) wording patterns.
        record Cand(int start, int end, String key, String category, boolean alerting) {}
        List<Cand> cands = new ArrayList<>();
        for (CrisisDetectionService.PhraseHit h : detector.findPhrases(text)) {
            boolean alerting = h.severity() == Severity.CRITICAL
                    || source != Source.AI_CHAT
                    || AiCrisisEscalationService.isChatAlertPhrase(h.phrase());
            cands.add(new Cand(h.start(), h.end(), h.phrase(), CATEGORY.getOrDefault(h.phrase(), "crisis language"), alerting));
        }
        if (source == Source.AI_CHAT) {
            for (AiAssistantService.PatternHit h : AiAssistantService.crisisPatternHits(text)) {
                int[] w = toWordBounds(text, h.start(), h.end());
                cands.add(new Cand(w[0], w[1], null,
                        h.indirect() ? "indirect suicidal thinking" : "suicidal thinking or self-harm", true));
            }
        }
        // Merge overlapping matches into one span. "I want to kill myself" hits both "i want to kill"
        // (harm to others) and "kill myself"; the merged span takes the most specific category.
        cands.sort(Comparator.comparingInt(Cand::start).thenComparing(c -> -(c.end() - c.start())));
        List<Cand> kept = new ArrayList<>();
        for (Cand c : cands) {
            Cand last = kept.isEmpty() ? null : kept.get(kept.size() - 1);
            if (last != null && c.start() < last.end()) {
                String cat = CATEGORY_PRIORITY.indexOf(c.category()) < CATEGORY_PRIORITY.indexOf(last.category())
                        ? c.category() : last.category();
                kept.set(kept.size() - 1, new Cand(last.start(), Math.max(last.end(), c.end()), null, cat,
                        last.alerting() || c.alerting()));
            } else {
                kept.add(c);
            }
        }

        List<Span> spans = new ArrayList<>();
        for (Cand c : kept) {
            String shown = text.substring(c.start(), c.end());
            Severity without = rate.apply(removeAll(text, shown));
            String role;
            String effect;
            if (!c.alerting()) {
                role = "context";
                effect = "Noted, but does not raise an alert on its own.";
            } else if (without.ordinal() < rating.ordinal()) {
                role = "decisive";
                effect = without == Severity.NONE
                        ? "Without this, the message would not have been flagged."
                        : "Without this, the rating would drop to " + without.name() + ".";
            } else {
                role = "supporting";
                effect = "Other phrases would still have flagged the message.";
            }
            spans.add(new Span(c.start(), c.end(), shown, c.category(), role, without.name(), effect));
        }

        List<String> categories = spans.stream().filter(s -> !"context".equals(s.role()))
                .map(Span::category).distinct().toList();
        return new Explanation(text, rating.name(), spans, categories, whyFlagged(rating, spans), true);
    }

    /** Explanation from the stored keywords alone, for older alerts whose text is gone. */
    public Explanation fromKeywords(List<String> keywords, String severity) {
        List<String> cats = keywords.stream().map(k -> CATEGORY.getOrDefault(k, "crisis language")).distinct().toList();
        String why = keywords.isEmpty() ? "Flagged " + severity + "."
                : "Flagged " + severity + ": contained " + quoteList(keywords) + ". The original text is no longer available.";
        return new Explanation(null, severity, List.of(), cats, why, false);
    }

    private Function<String, Severity> scannerFor(Source source) {
        if (source == Source.AI_CHAT) return t -> chatScanner.scan(t).severity();
        return t -> detector.scan(t).severity();
    }

    private static String whyFlagged(Severity rating, List<Span> spans) {
        List<Span> triggers = spans.stream().filter(s -> !"context".equals(s.role())).toList();
        if (rating == Severity.NONE || triggers.isEmpty()) {
            return "The current detector no longer flags this text (the rules may have changed since the alert was raised).";
        }
        StringBuilder sb = new StringBuilder("Flagged ").append(rating.name()).append(": contained ");
        List<String> parts = new ArrayList<>();
        for (Span s : triggers) parts.add("'" + s.text() + "' (" + s.category() + ")");
        sb.append(joinNatural(parts)).append('.');
        List<String> decisive = triggers.stream().filter(s -> "decisive".equals(s.role())).map(Span::text).toList();
        if (decisive.isEmpty() && triggers.size() > 1) {
            sb.append(" No single phrase decided it; several phrases each would have flagged it.");
        } else if (!decisive.isEmpty() && triggers.size() > 1) {
            sb.append(" Decisive: ").append(quoteList(decisive)).append('.');
        }
        return sb.toString();
    }

    private static String quoteList(List<String> items) {
        return joinNatural(items.stream().map(i -> "'" + i.strip() + "'").toList());
    }

    private static String joinNatural(List<String> parts) {
        if (parts.size() <= 1) return String.join("", parts);
        return String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
    }

    /** Removes every case-insensitive occurrence of the phrase (the occlusion test). */
    static String removeAll(String text, String phrase) {
        String lower = text.toLowerCase(Locale.ROOT);
        String p = phrase.toLowerCase(Locale.ROOT);
        if (p.isEmpty() || lower.length() != text.length()) return text.replace(phrase, " ");
        StringBuilder sb = new StringBuilder();
        int from = 0;
        while (true) {
            int i = lower.indexOf(p, from);
            if (i < 0) break;
            sb.append(text, from, i).append(' ');
            from = i + p.length();
        }
        sb.append(text.substring(from));
        return sb.toString();
    }

    /** Pattern matches like "suicid" are widened to whole words for display. */
    private static int[] toWordBounds(String text, int start, int end) {
        while (start > 0 && Character.isLetter(text.charAt(start - 1))) start--;
        while (end < text.length() && Character.isLetter(text.charAt(end))) end++;
        return new int[]{start, end};
    }
}
