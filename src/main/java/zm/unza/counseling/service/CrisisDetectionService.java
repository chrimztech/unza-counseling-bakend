package zm.unza.counseling.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CrisisDetectionService {

    public enum Severity { NONE, HIGH, CRITICAL }

    public record CrisisResult(Severity severity, List<String> triggeredKeywords) {
        public boolean isCrisis() { return severity != Severity.NONE; }
    }

    // Phrases that match a full substring (order: most severe first)
    private static final List<String> CRITICAL_PHRASES = Arrays.asList(
        "kill myself", "end my life", "take my own life", "want to die",
        "planning to kill", "going to kill", "i want to kill",
        "commit suicide", "suicidal", "suicide",
        "self harm", "self-harm", "selfharm", "cutting myself",
        "hurting myself", "hurt myself", "harming myself",
        "overdose", "hang myself", "drown myself", "poison myself",
        "slit my wrist", "slit wrists",
        "no reason to live", "life is not worth", "better off dead",
        "kill him", "kill her", "kill them", "kill you",
        "harm someone", "hurt someone", "attack someone",
        "bomb", "bombs", "weapon", "weapons", "gun", "guns", "knife to",
        "rape", "raped", "sexual assault"
    );

    private static final List<String> HIGH_PHRASES = Arrays.asList(
        "abuse", "abusing me", "being abused",
        "domestic violence", "beaten", "hitting me",
        "drug", "drugs", "alcohol", "substance",
        "depression", "hopeless", "helpless", "worthless", "useless",
        "can't go on", "cannot go on", "can not go on",
        "no point", "give up on life", "end it all",
        "anxiety attack", "panic attack", "breakdown",
        "trauma", "ptsd"
    );

    /**
     * Whole-word match. Plain substring matching raised CRITICAL alerts for everyday words —
     * "begun" (gun), "bombed my exam" (bomb), "grapes" (rape) — so each phrase must start and end
     * at a word boundary. Inflections that should still match are listed explicitly ("drugs", "guns").
     */
    private static boolean containsPhrase(String lower, String phrase) {
        int from = 0;
        while (true) {
            int i = lower.indexOf(phrase, from);
            if (i < 0) return false;
            int end = i + phrase.length();
            boolean startOk = i == 0 || !Character.isLetterOrDigit(lower.charAt(i - 1));
            boolean endOk = end == lower.length() || !Character.isLetterOrDigit(lower.charAt(end));
            if (startOk && endOk) return true;
            from = i + 1;
        }
    }

    /**
     * Scans one or more text fields and returns the highest severity found plus all triggered phrases.
     */
    public CrisisResult scan(String... texts) {
        List<String> triggered = new ArrayList<>();
        Severity highest = Severity.NONE;

        for (String text : texts) {
            if (text == null || text.isBlank()) continue;
            String lower = text.toLowerCase(Locale.ROOT).replace('’', '\'');

            for (String phrase : CRITICAL_PHRASES) {
                if (containsPhrase(lower, phrase) && !triggered.contains(phrase)) {
                    triggered.add(phrase);
                    highest = Severity.CRITICAL;
                }
            }

            if (highest != Severity.CRITICAL) {
                for (String phrase : HIGH_PHRASES) {
                    if (containsPhrase(lower, phrase) && !triggered.contains(phrase)) {
                        triggered.add(phrase);
                        if (highest == Severity.NONE) highest = Severity.HIGH;
                    }
                }
            }
        }

        return new CrisisResult(highest, triggered);
    }
}
