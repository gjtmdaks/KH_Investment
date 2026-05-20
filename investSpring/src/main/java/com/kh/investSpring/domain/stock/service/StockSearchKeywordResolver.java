package com.kh.investSpring.domain.stock.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.stock.dto.StockKeywordSearchDto;

/**
 * 종목 검색어를 DB 조회에 적합한 키워드 집합으로 확장합니다.
 * 영문 약어(sk, lg), 별칭(삼전), 부분 입력(삼성) 등을 한글 종목명 후보로 변환합니다.
 */
@Component
public class StockSearchKeywordResolver {

    private static final Pattern STOCK_NAME_SUFFIX =
            Pattern.compile("(?:보통주|[0-9]?우선주[A-Z]?)$");

    private static final Set<String> ENGLISH_ONLY_ABBREVS = Set.of(
            "sk", "lg", "kb", "kt", "hd", "cj", "gs", "dl", "db", "ai", "posco"
    );

    private static final Map<String, String> EXACT_ALIASES = Map.ofEntries(
            Map.entry("삼전", "삼성전자"),
            Map.entry("삼성 전자", "삼성전자"),
            Map.entry("sk하이닉스", "에스케이하이닉스"),
            Map.entry("하닉", "에스케이하이닉스"),
            Map.entry("sk hynix", "에스케이하이닉스"),
            Map.entry("skhynix", "에스케이하이닉스"),
            Map.entry("하이닉스", "에스케이하이닉스"),
            Map.entry("sk", "에스케이하이닉스"),
            Map.entry("에스", "에스케이하이닉스"),
            Map.entry("현대자동차", "현대차"),
            Map.entry("현차", "현대차"),
            Map.entry("현대", "현대차"),
            Map.entry("기아차", "기아"),
            Map.entry("lg엔솔", "LG에너지솔루션"),
            Map.entry("naver", "네이버"),
            Map.entry("posco", "포스코홀딩스"),
            Map.entry("포스코", "포스코홀딩스"),
            Map.entry("삼바", "삼성바이오로직스"),
            Map.entry("한전", "한국전력"),
            Map.entry("케이티", "KT"),
            Map.entry("skt", "SK텔레콤"),
            Map.entry("전기", "삼성전기"),
            Map.entry("두산", "두산에너빌리티"),
            Map.entry("두산에너", "두산에너빌리티"),
            Map.entry("HD", "현대중공업"),
            Map.entry("현대중", "현대중공업"),
            Map.entry("바이오", "삼성바이오로직스"),
            Map.entry("효성", "효성중공업"),
            Map.entry("삼천", "삼천당제약"),
            Map.entry("롯데", "롯데케미칼"),
            Map.entry("주성", "주성엔지니어링"),
            Map.entry("코스모", "코스모로보틱스"),
            Map.entry("광", "광전자"),
            Map.entry("빛", "빛과전자"),
            Map.entry("모비스", "현대모비스"),
            Map.entry("대우", "대우건설"),
            Map.entry("제주", "제주반도체"),
            Map.entry("한미", "한미반도체"),
            Map.entry("미래", "미래에셋증권"),
            Map.entry("삼성", "삼성SDI"),
            Map.entry("LG", "LG전자"),
            Map.entry("이노", "이노인스트루먼트"),
            Map.entry("아이", "아이로보틱스"),
            Map.entry("대한", "대한전선"),
            Map.entry("한화", "한화오션")
            
             
    );

    public record ResolvedQuery(
            String primaryKeyword,
            List<String> keywords,
            List<String> boostCanonicalNames,
            boolean searchStockCode
    ) {
        static ResolvedQuery empty() {
            return new ResolvedQuery("", List.of(), List.of(), false);
        }
    }

    public ResolvedQuery resolve(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return ResolvedQuery.empty();
        }

        String primary = rawKeyword.trim();
        String compact = primary.replace(" ", "");
        String lower = compact.toLowerCase(Locale.ROOT);

        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        LinkedHashSet<String> boost = new LinkedHashSet<>();

        keywords.add(primary);
        if (!compact.equals(primary)) {
            keywords.add(compact);
        }

        String aliasTarget = EXACT_ALIASES.get(lower);
        if (aliasTarget == null) {
            aliasTarget = EXACT_ALIASES.get(primary);
        }
        if (aliasTarget != null) {
            keywords.add(aliasTarget);
            boost.add(aliasTarget);
        }

        expandEnglishAbbrev(lower, keywords, boost);
        expandPartialBrand(lower, keywords, boost);

        if (ENGLISH_ONLY_ABBREVS.contains(lower)) {
            keywords.remove(primary);
            keywords.remove(compact);
            keywords.remove(lower);
        }

        boolean searchStockCode = shouldSearchStockCode(primary, compact, lower);

        return new ResolvedQuery(
                primary,
                List.copyOf(keywords),
                List.copyOf(boost),
                searchStockCode
        );
    }

    public static String stripStockNameSuffix(String stockName) {
        if (stockName == null) {
            return "";
        }
        return STOCK_NAME_SUFFIX.matcher(stockName.trim()).replaceFirst("").trim();
    }

    private static void expandEnglishAbbrev(
            String lower,
            LinkedHashSet<String> keywords,
            LinkedHashSet<String> boost
    ) {
        switch (lower) {
            case "sk" -> {
                keywords.add("에스케이");
                keywords.add("하이닉스");
                boost.add("에스케이하이닉스");
            }
            case "lg" -> keywords.add("엘지");
            case "kb" -> keywords.add("케이비");
            case "kt" -> keywords.add("케이티");
            case "hd" -> keywords.add("현대");
            case "cj" -> keywords.add("씨제이");
            case "gs" -> keywords.add("지에스");
            case "dl" -> keywords.add("디엘");
            case "db" -> keywords.add("디비");
            case "posco" -> {
                keywords.add("포스코");
                boost.add("포스코홀딩스");
            }
            default -> {
                if (lower.startsWith("sk") && lower.length() <= 12) {
                    keywords.add("에스케이");
                    if (lower.contains("하이닉") || lower.contains("hynix") || lower.equals("sk")) {
                        keywords.add("하이닉스");
                        boost.add("에스케이하이닉스");
                    }
                }
                if (lower.startsWith("lg") && lower.length() <= 10) {
                    keywords.add("엘지");
                }
                if (lower.startsWith("kb") && lower.length() <= 8) {
                    keywords.add("케이비");
                }
            }
        }
    }

    private static void expandPartialBrand(
            String lower,
            LinkedHashSet<String> keywords,
            LinkedHashSet<String> boost
    ) {
        if (lower.equals("삼성") || (lower.startsWith("삼성") && lower.length() <= 6)) {
            boost.add("삼성전자");
            keywords.add("삼성전자");
        }
        if (lower.equals("현대") || (lower.startsWith("현대") && lower.length() <= 4)) {
            boost.add("현대차");
        }
        if (lower.equals("카카") || lower.startsWith("카카")) {
            boost.add("카카오");
        }
        if (lower.equals("네이") || lower.equals("naver")) {
            boost.add("네이버");
        }
        if (lower.equals("셀트") || lower.startsWith("셀트")) {
            boost.add("셀트리온");
        }
    }

    private static boolean shouldSearchStockCode(String primary, String compact, String lower) {
        if (compact.matches("\\d{4,8}")) {
            return true;
        }
        if (ENGLISH_ONLY_ABBREVS.contains(lower)) {
            return false;
        }
        return compact.length() >= 3 && compact.matches("[a-z0-9]+");
    }

    public static List<StockKeywordSearchDto> dedupePreferCommonStock(List<StockKeywordSearchDto> hits) {
        if (hits == null || hits.isEmpty()) {
            return List.of();
        }
        Map<String, StockKeywordSearchDto> bestByCanonical = new java.util.LinkedHashMap<>();
        for (StockKeywordSearchDto hit : hits) {
            if (hit == null || hit.getStockName() == null) {
                continue;
            }
            String canonical = stripStockNameSuffix(hit.getStockName());
            StockKeywordSearchDto existing = bestByCanonical.get(canonical);
            if (existing == null || preferOver(existing, hit)) {
                bestByCanonical.put(canonical, hit);
            }
        }
        return new ArrayList<>(bestByCanonical.values());
    }

    private static boolean preferOver(StockKeywordSearchDto current, StockKeywordSearchDto challenger) {
        boolean currentCommon = current.getStockName() != null && current.getStockName().endsWith("보통주");
        boolean challengerCommon = challenger.getStockName() != null && challenger.getStockName().endsWith("보통주");
        if (challengerCommon && !currentCommon) {
            return true;
        }
        if (currentCommon && !challengerCommon) {
            return false;
        }
        int currentScore = current.getMatchScore() != null ? current.getMatchScore() : 0;
        int challengerScore = challenger.getMatchScore() != null ? challenger.getMatchScore() : 0;
        if (challengerScore != currentScore) {
            return challengerScore > currentScore;
        }
        long currentTv = current.getTradingValue() != null ? current.getTradingValue() : 0L;
        long challengerTv = challenger.getTradingValue() != null ? challenger.getTradingValue() : 0L;
        return challengerTv > currentTv;
    }
}
