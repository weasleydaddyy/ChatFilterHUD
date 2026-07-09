package com.chatfilterhud.filter;

import com.chatfilterhud.ChatFilterHUDMod;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordFilter {
    private List<String> bannedWords = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();
    private List<Pattern> fuzzyPatterns = new ArrayList<>();

    /**
     * Leetspeak character mappings — each char maps to a regex class
     * that matches the original letter, its leet variants, and separator patterns.
     */
    private static final java.util.Map<Character, String> LEET_MAP = new java.util.HashMap<>();
    static {
        // a: a, @, 4, /\, /-\
        LEET_MAP.put('a', "[a@4\\/\\\\-]");
        // b: b, 8, |3, 13
        LEET_MAP.put('b', "[b8|313]");
        // c: c, (, <, {, k
        LEET_MAP.put('c', "[c(<{k]");
        // d: d, |), |}
        LEET_MAP.put('d', "[d\\)|\\}]");
        // e: e, 3, &
        LEET_MAP.put('e', "[e3&]");
        // f: f, |=, ph
        LEET_MAP.put('f', "[f|=ph]");
        // g: g, 6, 9, &
        LEET_MAP.put('g', "[g69&]");
        // h: h, #, |-|
        LEET_MAP.put('h', "[h#\\|-\\|]");
        // i: i, 1, !, |
        LEET_MAP.put('i', "[i1!\\|]");
        // j: j, _|
        LEET_MAP.put('j', "[j_\\|]");
        // k: k, |<
        LEET_MAP.put('k', "[k|<]");
        // l: l, 1, |_, /
        LEET_MAP.put('l', "[l1|_\\/]");
        // m: m, |\/|, [V]
        LEET_MAP.put('m', "[m|\\/\\|[V\\]]");
        // n: n, |\|, /\/
        LEET_MAP.put('n', "[n|\\|\\/\\/]");
        // o: o, 0, (), []
        LEET_MAP.put('o', "[o0()\\[\\]]");
        // p: p, |*, |>
        LEET_MAP.put('p', "[p|*|>]");
        // q: q, 0_, ()
        LEET_MAP.put('q', "[q0_()]");
        // r: r, |2, 12
        LEET_MAP.put('r', "[r|212]");
        // s: s, 5, $, z
        LEET_MAP.put('s', "[s5$z]");
        // t: t, 7, +
        LEET_MAP.put('t', "[t7+]");
        // u: u, |_|, v
        LEET_MAP.put('u', "[u|_\\|v]");
        // v: v, \/, |
        LEET_MAP.put('v', "[v\\/\\|]");
        // w: w, \/\/, uu
        LEET_MAP.put('w', "[w\\/\\/uu]");
        // x: x, %, ><
        LEET_MAP.put('x', "[x%><]");
        // y: y, 7, j
        LEET_MAP.put('y', "[y7j]");
        // z: z, 2, 7_
        LEET_MAP.put('z', "[z27_]");
    }

    public WordFilter(List<String> bannedWords) {
        updateBannedWords(bannedWords);
    }

    public void updateBannedWords(List<String> words) {
        this.bannedWords = new ArrayList<>(words);
        this.patterns = words.stream()
                .filter(w -> !w.isEmpty())
                .map(word -> Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .collect(Collectors.toList());

        // Build fuzzy patterns: between each letter allow zero or more
        // separator characters (spaces, dots, slashes, underscores, hyphens, etc.)
        // and leet variants of the letter
        this.fuzzyPatterns = words.stream()
                .filter(w -> !w.isEmpty())
                .map(WordFilter::buildFuzzyPattern)
                .collect(Collectors.toList());
    }

    /**
     * Build a regex pattern that matches the word with:
     * - Leet substitutions (0→o, 3→e, 4→a, 5→s, $→s, 7→t, 1→i/l, etc.)
     * - Separator characters between letters (., /, \, |, _, -, space, etc.)
     * - Case insensitive
     */
    private static Pattern buildFuzzyPattern(String word) {
        StringBuilder sb = new StringBuilder();
        sb.append("(?iu)"); // case-insensitive + unicode
        String lower = word.toLowerCase(java.util.Locale.ROOT);

        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            String leetClass = LEET_MAP.getOrDefault(c, "[" + Pattern.quote(String.valueOf(c)) + "]");
            sb.append("(?:");
            sb.append(leetClass);
            sb.append(")");
            // After each character, allow zero or more separator characters
            if (i < lower.length() - 1) {
                sb.append("[\\s\\.\\\\/\\|_\\-\\+\\*]*");
            }
        }

        return Pattern.compile(sb.toString());
    }

    public boolean containsBannedWord(String message) {
        if (message == null || message.isEmpty()) return false;

        boolean useFuzzy = ChatFilterHUDMod.getInstance() != null
                && ChatFilterHUDMod.getInstance().getConfig().isFuzzyMatching();

        // Check exact patterns first
        for (Pattern pattern : patterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }

        // If fuzzy matching is enabled, check leet/separator variants
        if (useFuzzy) {
            for (Pattern pattern : fuzzyPatterns) {
                if (pattern.matcher(message).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    public Text highlightBannedWords(Text original) {
        String plain = original.getString();
        if (plain.isEmpty()) return original;

        boolean useFuzzy = ChatFilterHUDMod.getInstance() != null
                && ChatFilterHUDMod.getInstance().getConfig().isFuzzyMatching();

        List<int[]> ranges = new ArrayList<>();

        // Check exact patterns
        for (Pattern pattern : patterns) {
            java.util.regex.Matcher matcher = pattern.matcher(plain);
            while (matcher.find()) {
                ranges.add(new int[]{matcher.start(), matcher.end()});
            }
        }

        // Check fuzzy patterns
        if (useFuzzy) {
            for (Pattern pattern : fuzzyPatterns) {
                java.util.regex.Matcher matcher = pattern.matcher(plain);
                while (matcher.find()) {
                    ranges.add(new int[]{matcher.start(), matcher.end()});
                }
            }
        }

        if (ranges.isEmpty()) return original;

        ranges.sort((a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        for (int[] range : ranges) {
            if (merged.isEmpty()) {
                merged.add(new int[]{range[0], range[1]});
            } else {
                int[] last = merged.get(merged.size() - 1);
                if (range[0] <= last[1]) {
                    last[1] = Math.max(last[1], range[1]);
                } else {
                    merged.add(new int[]{range[0], range[1]});
                }
            }
        }

        MutableText result = Text.literal("");
        int lastEnd = 0;
        for (int[] range : merged) {
            if (range[0] > lastEnd) {
                result.append(Text.literal(plain.substring(lastEnd, range[0])));
            }
            result.append(Text.literal(plain.substring(range[0], range[1]))
                    .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true)));
            lastEnd = range[1];
        }
        if (lastEnd < plain.length()) {
            result.append(Text.literal(plain.substring(lastEnd)));
        }

        return result;
    }
}