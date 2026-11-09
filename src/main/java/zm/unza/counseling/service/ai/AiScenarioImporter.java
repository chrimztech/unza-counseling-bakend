package zm.unza.counseling.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import zm.unza.counseling.entity.AiCategoryGuidance;
import zm.unza.counseling.entity.AiCounsellingScenario;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parses the "AI Counselling Dialogue Template" workbook (any of its versions).
 *
 * <ul>
 *   <li>Row-wise sheets (e.g. "Data Entry"): the header row is located by searching the first
 *       rows for a "Student_Message" cell — some versions have a dummy A/B/C row above it.</li>
 *   <li>Transposed sheets (e.g. "Example (One Completed Row)"): field names run down column A and
 *       each further column is one scenario. These hand-written examples are flagged gold.</li>
 *   <li>"Guidance_Examples": Problem Category → Suggested AI Response Direction.</li>
 * </ul>
 *
 * Contributor-name columns are skipped on purpose (the counsellor instructions promise
 * contributors anonymity), as are student demographics, which the model doesn't need.
 * Columns are matched by header text, so the actual meaning of the template's mislabelled
 * columns is encoded in {@link #fieldFor(String)}.
 */
@Slf4j
@Component
public class AiScenarioImporter {

    enum Field { CODE, CATEGORY, RISK, MESSAGE, RESPONSE, ESCALATION, TOPICS, NOTES, SOURCE, REVIEW, IGNORE, EXTRA }

    private static final int MAX_COLS = 30;
    private static final int HEADER_SEARCH_ROWS = 6;
    private static final Pattern WS = Pattern.compile("\\s+");
    private static final Pattern CONTRACTION = Pattern.compile("(?<=\\p{L})\uFFFD(?=(t|s|m|d|ll|re|ve)\\b)");

    private static final Map<String, String> CANONICAL_CATEGORIES = new HashMap<>();
    static {
        for (String c : List.of("Financial", "Academic", "Relationship", "Emotional/Distress", "Crisis/Suicidal",
                "Accommodation", "Trauma/GBV", "Health", "Family", "Substance Use", "Career", "Other", "Mental Health",
                "Academic/Financial", "Family/Financial", "Academic/Accessibility", "Safety/Sexual Harassment")) {
            CANONICAL_CATEGORIES.put(c.toLowerCase(Locale.ROOT), c);
        }
    }

    public record ImportResult(List<AiCounsellingScenario> scenarios, List<AiCategoryGuidance> guidance,
                               int duplicatesSkipped, int emptyRowsSkipped, List<String> sheetsRead, List<String> warnings) {}

    public ImportResult parse(InputStream in, String fileName) throws Exception {
        List<AiCounsellingScenario> gold = new ArrayList<>();
        List<AiCounsellingScenario> rows = new ArrayList<>();
        List<AiCategoryGuidance> guidance = new ArrayList<>();
        List<String> sheetsRead = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int[] empty = {0};

        DataFormatter fmt = new DataFormatter(Locale.ROOT);
        try (Workbook wb = WorkbookFactory.create(in)) {
            for (Sheet sheet : wb) {
                List<List<String>> grid = readGrid(sheet, fmt);
                if (grid.isEmpty()) continue;

                int headerRow = findHeaderRow(grid);
                if (headerRow >= 0) {
                    int before = rows.size();
                    parseRowWise(grid, headerRow, fileName, rows, empty);
                    sheetsRead.add(sheet.getSheetName() + " (" + (rows.size() - before) + " rows)");
                    continue;
                }
                if (isTransposed(grid)) {
                    int before = gold.size();
                    parseTransposed(grid, fileName, gold);
                    sheetsRead.add(sheet.getSheetName() + " (" + (gold.size() - before) + " examples)");
                    continue;
                }
                int gHeader = findGuidanceHeader(grid);
                if (gHeader >= 0) {
                    int before = guidance.size();
                    parseGuidance(grid, gHeader, guidance);
                    sheetsRead.add(sheet.getSheetName() + " (" + (guidance.size() - before) + " guidance rules)");
                }
                // Anything else (instructions, summaries) is intentionally ignored.
            }
        }

        // Gold examples first so a duplicate in Data Entry doesn't displace the curated version.
        List<AiCounsellingScenario> all = new ArrayList<>(gold);
        all.addAll(rows);
        Set<String> seen = new HashSet<>();
        List<AiCounsellingScenario> unique = new ArrayList<>();
        int dupes = 0;
        for (AiCounsellingScenario s : all) {
            String key = s.getStudentMessage().toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
            if (!seen.add(key)) { dupes++; continue; }
            unique.add(s);
        }
        if (unique.isEmpty()) {
            warnings.add("No scenarios found. The workbook needs a sheet with a 'Student_Message' column.");
        }
        return new ImportResult(unique, guidance, dupes, empty[0], sheetsRead, warnings);
    }

    // ------------------------------------------------------------------------------- layouts

    private static void parseRowWise(List<List<String>> grid, int headerRow, String fileName,
                                     List<AiCounsellingScenario> out, int[] empty) {
        List<String> header = grid.get(headerRow);
        // The header row is often physically shorter than data rows (trailing blank header cells),
        // so size by the widest row and treat columns beyond the header as unlabelled.
        int width = grid.stream().mapToInt(List::size).max().orElse(header.size());
        Field[] fields = new Field[width];
        for (int c = 0; c < width; c++) fields[c] = c < header.size() ? fieldFor(header.get(c)) : Field.EXTRA;

        for (int r = headerRow + 1; r < grid.size(); r++) {
            List<String> row = grid.get(r);
            Map<Field, String> values = new EnumMap<>(Field.class);
            String extra = null;
            for (int c = 0; c < Math.min(row.size(), fields.length); c++) {
                String v = row.get(c);
                if (v == null || v.isBlank()) continue;
                if (fields[c] == Field.EXTRA) { if (extra == null && v.trim().length() > 40) extra = v; continue; }
                if (fields[c] != Field.IGNORE) values.putIfAbsent(fields[c], v);
            }
            // A few rows in the template carry the response in an unlabelled column instead.
            if (!values.containsKey(Field.RESPONSE) && extra != null) values.put(Field.RESPONSE, extra);
            AiCounsellingScenario s = toScenario(values, fileName, false);
            if (s == null) { if (!values.isEmpty()) empty[0]++; continue; }
            out.add(s);
        }
    }

    private static void parseTransposed(List<List<String>> grid, String fileName, List<AiCounsellingScenario> out) {
        Map<Integer, Field> rowFields = new LinkedHashMap<>();
        int maxCols = 0;
        for (int r = 0; r < grid.size(); r++) {
            List<String> row = grid.get(r);
            maxCols = Math.max(maxCols, row.size());
            if (row.isEmpty() || row.get(0) == null) continue;
            Field f = fieldFor(row.get(0));
            if (f != Field.IGNORE && f != Field.EXTRA) rowFields.put(r, f);
        }
        for (int c = 1; c < maxCols; c++) {
            Map<Field, String> values = new EnumMap<>(Field.class);
            for (Map.Entry<Integer, Field> e : rowFields.entrySet()) {
                List<String> row = grid.get(e.getKey());
                if (c < row.size() && row.get(c) != null && !row.get(c).isBlank()) values.putIfAbsent(e.getValue(), row.get(c));
            }
            AiCounsellingScenario s = toScenario(values, fileName, true);
            if (s != null) out.add(s);
        }
    }

    private static void parseGuidance(List<List<String>> grid, int headerRow, List<AiCategoryGuidance> out) {
        List<String> header = grid.get(headerRow);
        int cat = -1, open = -1, dir = -1;
        for (int c = 0; c < header.size(); c++) {
            String k = key(header.get(c));
            if (k.startsWith("problemcategory") || k.equals("category")) cat = c;
            else if (k.contains("opening") || k.contains("example")) open = c;
            else if (k.contains("direction") || k.contains("response")) dir = c;
        }
        if (cat < 0 || dir < 0) return;
        for (int r = headerRow + 1; r < grid.size(); r++) {
            List<String> row = grid.get(r);
            String category = cell(row, cat);
            String direction = cell(row, dir);
            if (category == null || direction == null) continue;
            AiCategoryGuidance g = new AiCategoryGuidance();
            g.setProblemCategory(normaliseCategory(category));
            g.setExampleOpening(truncate(clean(cell(row, open)), 500));
            g.setResponseDirection(clean(direction));
            g.setSortOrder(out.size());
            out.add(g);
        }
    }

    // ------------------------------------------------------------------------------- detection

    private static int findHeaderRow(List<List<String>> grid) {
        for (int r = 0; r < Math.min(HEADER_SEARCH_ROWS, grid.size()); r++) {
            List<String> row = grid.get(r);
            // A real header row has Student_Message in a column other than A for transposed sheets,
            // or alongside other recognised headers.
            long recognised = row.stream().filter(Objects::nonNull).map(AiScenarioImporter::fieldFor)
                    .filter(f -> f != Field.IGNORE && f != Field.EXTRA).count();
            boolean hasMessage = row.stream().filter(Objects::nonNull).anyMatch(h -> fieldFor(h) == Field.MESSAGE);
            if (hasMessage && recognised >= 3) return r;
        }
        return -1;
    }

    private static boolean isTransposed(List<List<String>> grid) {
        long recognised = grid.stream().limit(30)
                .map(row -> row.isEmpty() ? null : row.get(0)).filter(Objects::nonNull)
                .map(AiScenarioImporter::fieldFor).filter(f -> f != Field.IGNORE && f != Field.EXTRA).count();
        boolean hasMessage = grid.stream().limit(30)
                .anyMatch(row -> !row.isEmpty() && row.get(0) != null && fieldFor(row.get(0)) == Field.MESSAGE);
        return hasMessage && recognised >= 3;
    }

    private static int findGuidanceHeader(List<List<String>> grid) {
        for (int r = 0; r < Math.min(HEADER_SEARCH_ROWS, grid.size()); r++) {
            List<String> keys = grid.get(r).stream().map(AiScenarioImporter::key).toList();
            boolean cat = keys.stream().anyMatch(k -> k.startsWith("problemcategory"));
            boolean dir = keys.stream().anyMatch(k -> k.contains("direction"));
            if (cat && dir) return r;
        }
        return -1;
    }

    /** Maps a header label to what the column really contains in the template. */
    static Field fieldFor(String header) {
        String k = key(header);
        if (k.isEmpty()) return Field.EXTRA;
        if (k.equals("scenarioid") || k.equals("id")) return Field.CODE;
        if (k.startsWith("problemcategory") || k.equals("category")) return Field.CATEGORY;
        if (k.startsWith("risklevel") || k.equals("risk")) return Field.RISK;
        if (k.startsWith("studentmessage")) return Field.MESSAGE;
        // "Ideal_AI_Response        Key_Topics" is one merged header over the response column.
        if (k.startsWith("idealairesponse") || k.startsWith("idealresponse") || k.equals("airesponse")) return Field.RESPONSE;
        // In the Data Entry sheet "Escalation_Needed 2" actually holds the topics/themes.
        if (k.startsWith("keytopic") || k.startsWith("escalationneeded2") || k.equals("topics")) return Field.TOPICS;
        if (k.startsWith("escalation") || k.startsWith("referral")) return Field.ESCALATION;
        if (k.startsWith("notesforai") || k.equals("notes")) return Field.NOTES;
        if (k.equals("responsesource")) return Field.SOURCE;              // "Counsellor" / "AI draft - ..."
        if (k.equals("aireviewnote") || k.equals("reviewnote")) return Field.REVIEW;
        return Field.IGNORE; // names, year, gender, residence, "Column", "Content", ...
    }

    // ------------------------------------------------------------------------------- mapping

    private static AiCounsellingScenario toScenario(Map<Field, String> v, String fileName, boolean gold) {
        String message = clean(v.get(Field.MESSAGE));
        if (message == null || message.length() < 8) return null;
        AiCounsellingScenario s = new AiCounsellingScenario();
        s.setScenarioCode(truncate(clean(v.get(Field.CODE)), 50));
        s.setProblemCategory(truncate(normaliseCategory(v.get(Field.CATEGORY)), 100));
        s.setRiskLevel(normaliseRisk(v.get(Field.RISK)));
        s.setStudentMessage(message);
        s.setIdealResponse(clean(v.get(Field.RESPONSE)));
        s.setEscalation(truncate(clean(v.get(Field.ESCALATION)), 500));
        s.setKeyTopics(truncate(clean(v.get(Field.TOPICS)), 500));
        s.setNotesForAi(clean(v.get(Field.NOTES)));
        String source = v.get(Field.SOURCE);
        s.setAiDrafted(source != null && source.trim().toLowerCase(Locale.ROOT).matches("^ai\\b.*"));
        s.setReviewNote(truncate(clean(v.get(Field.REVIEW)), 500));
        s.setGoldExample(gold);
        s.setSource(AiCounsellingScenario.SOURCE_IMPORT);
        s.setImportFile(truncate(fileName, 255));
        s.setEnabled(true);
        return s;
    }

    static String normaliseRisk(String raw) {
        if (raw == null) return null;
        String r = raw.trim().toLowerCase(Locale.ROOT);
        if (r.contains("critical")) return "CRITICAL";
        if (r.contains("high")) return "HIGH";
        if (r.contains("moderate") || r.contains("medium")) return "MODERATE";
        if (r.contains("low")) return "LOW";
        return null;
    }

    static String normaliseCategory(String raw) {
        String c = clean(raw);
        if (c == null) return null;
        String canonical = CANONICAL_CATEGORIES.get(c.toLowerCase(Locale.ROOT));
        if (canonical != null) return canonical;
        return c.equals(c.toLowerCase(Locale.ROOT)) ? Character.toUpperCase(c.charAt(0)) + c.substring(1) : c;
    }

    /** Trims, collapses whitespace and repairs the template's lost smart quotes/dashes (U+FFFD). */
    static String clean(String raw) {
        if (raw == null) return null;
        String s = CONTRACTION.matcher(raw).replaceAll("'");
        s = s.replace("\uFFFD", " — ");
        s = WS.matcher(s).replaceAll(" ").trim();
        return s.isEmpty() ? null : s;
    }

    // ------------------------------------------------------------------------------- POI helpers

    private static List<List<String>> readGrid(Sheet sheet, DataFormatter fmt) {
        List<List<String>> grid = new ArrayList<>();
        int last = sheet.getLastRowNum();
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            List<String> cells = new ArrayList<>();
            if (row != null) {
                int lastCell = Math.min(row.getLastCellNum(), MAX_COLS);
                for (int c = 0; c < lastCell; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String v = cell == null ? null : fmt.formatCellValue(cell);
                    cells.add(v == null || v.isBlank() ? null : v);
                }
            }
            grid.add(cells);
        }
        return grid;
    }

    private static String cell(List<String> row, int c) {
        return c >= 0 && c < row.size() ? row.get(c) : null;
    }

    private static String key(String header) {
        return header == null ? "" : header.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
