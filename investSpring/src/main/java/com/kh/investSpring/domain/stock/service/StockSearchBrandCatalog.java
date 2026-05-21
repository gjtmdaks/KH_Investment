package com.kh.investSpring.domain.stock.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 영문·한글 브랜드 접두 동의어를 관리합니다.
 * 예: LG(12종목) ↔ 엘지(2종목), SK(18) ↔ 에스케이(9), 케이티(9) ↔ KT(검색용)
 */
@Component
@Slf4j
public class StockSearchBrandCatalog {

    private static final String CSV_PATH = "STOCKS_DATA_TABLE.csv";

    private static final Pattern STOCK_NAME_SUFFIX =
            Pattern.compile("(?:보통주|[0-9]?우선주[A-Z]?)$");

    /**
     * CSV 종목명 접두 분석으로 정의한 브랜드 동의어 그룹.
     * KB↔케이비는 CSV상 별도 계열(케이비아이·SPAC)이라 제외.
     */
    private static final List<List<String>> BRAND_GROUP_DEFINITIONS = List.of(
            List.of("LG", "엘지"),
            List.of("SK", "에스케이"),
            List.of("KT", "케이티"),
            List.of("CJ", "씨제이"),
            List.of("GS", "지에스"),
            List.of("NAVER", "네이버"),
            List.of("POSCO", "포스코"),
            List.of("KB"),
            List.of("DB", "디비"),
            List.of("DL", "디엘")
    );

    private List<ActiveBrandGroup> activeGroups = List.of();

    @PostConstruct
    void load() {
        List<String> stockNames = readActiveStockNamesFromCsv();
        activeGroups = buildActiveGroups(stockNames);
        log.info(
                "종목 검색 브랜드 동의어 {}개 그룹 로드 (CSV {}건)",
                activeGroups.size(),
                stockNames.size()
        );
    }

    /**
     * 검색어가 브랜드 그룹에 해당하면 CSV에 존재하는 모든 표기( LG / 엘지 등 )를 keywords에 추가합니다.
     */
    public void expandBrandSynonyms(String lower, String compact, Set<String> keywords) {
        if (lower == null || lower.isBlank() || activeGroups.isEmpty()) {
            return;
        }
        for (ActiveBrandGroup group : activeGroups) {
            if (group.matchesQuery(lower, compact)) {
                keywords.addAll(group.searchTokens());
            }
        }
    }

    List<ActiveBrandGroup> getActiveGroups() {
        return activeGroups;
    }

    private List<String> readActiveStockNamesFromCsv() {
        List<String> names = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(CSV_PATH);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line == null) {
                    return names;
                }
                while ((line = reader.readLine()) != null) {
                    String name = parseStockNameColumn(line);
                    if (name != null && !name.isBlank()) {
                        names.add(name.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("종목 CSV({}) 로드 실패, 기본 브랜드 규칙만 사용: {}", CSV_PATH, e.getMessage());
        }
        return names;
    }

    private static String parseStockNameColumn(String csvLine) {
        if (csvLine == null || csvLine.isBlank()) {
            return null;
        }
        int firstComma = csvLine.indexOf(',');
        if (firstComma < 0) {
            return null;
        }
        int start = firstComma + 1;
        if (start >= csvLine.length()) {
            return null;
        }
        if (csvLine.charAt(start) == '"') {
            int end = csvLine.indexOf('"', start + 1);
            if (end < 0) {
                return null;
            }
            return csvLine.substring(start + 1, end);
        }
        int end = csvLine.indexOf(',', start);
        return end < 0 ? csvLine.substring(start) : csvLine.substring(start, end);
    }

    private List<ActiveBrandGroup> buildActiveGroups(List<String> stockNames) {
        List<ActiveBrandGroup> built = new ArrayList<>();
        for (List<String> definition : BRAND_GROUP_DEFINITIONS) {
            List<BrandVariant> variants = new ArrayList<>();
            for (String token : definition) {
                int count = countPrefix(stockNames, token);
                variants.add(new BrandVariant(token, count));
            }
            boolean anyPresent = variants.stream().anyMatch(v -> v.prefixCount() > 0);
            if (!anyPresent) {
                continue;
            }
            built.add(new ActiveBrandGroup(variants));
        }
        return List.copyOf(built);
    }

    private static int countPrefix(List<String> stockNames, String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return 0;
        }
        int count = 0;
        for (String rawName : stockNames) {
            String base = stripSuffix(rawName);
            if (base.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    private static String stripSuffix(String stockName) {
        return STOCK_NAME_SUFFIX.matcher(stockName.trim()).replaceFirst("").trim();
    }

    record BrandVariant(String searchToken, int prefixCount) {
        String matchKey() {
            return searchToken.toLowerCase(Locale.ROOT);
        }
    }

    static final class ActiveBrandGroup {
        private final List<BrandVariant> variants;
        private final List<String> searchTokens;

        ActiveBrandGroup(List<BrandVariant> variants) {
            this.variants = List.copyOf(variants);
            LinkedHashSet<String> tokens = new LinkedHashSet<>();
            for (BrandVariant variant : variants) {
                tokens.add(variant.searchToken());
            }
            this.searchTokens = List.copyOf(tokens);
        }

        List<String> searchTokens() {
            return searchTokens;
        }

        boolean matchesQuery(String lower, String compact) {
            for (BrandVariant variant : variants) {
                if (matchesVariant(lower, compact, variant)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean matchesVariant(String lower, String compact, BrandVariant variant) {
            String key = variant.matchKey();
            if (lower.equals(key) || compact.equalsIgnoreCase(variant.searchToken())) {
                return true;
            }
            if (!lower.startsWith(key)) {
                return false;
            }
            if (isShortLatin(key)) {
                return lower.length() <= 12;
            }
            return lower.length() <= key.length() + 24;
        }

        private static boolean isShortLatin(String key) {
            return key.length() <= 5 && key.chars().allMatch(ch -> ch < 128);
        }
    }
}
