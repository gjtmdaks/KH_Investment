package com.kh.investSpring.domain.ai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.domain.account.dao.AccountDao;
import com.kh.investSpring.domain.account.dto.AccountAssetResponse.HoldingStock;
import com.kh.investSpring.domain.account.dto.AccountAssetSummaryDto;
import com.kh.investSpring.domain.ai.dao.UserAiProfileDao;
import com.kh.investSpring.domain.ai.dto.InvestmentTypeDto;
import com.kh.investSpring.domain.ai.dto.UserAiProfileDto;
import com.kh.investSpring.domain.ai.dto.UserProfileAnalyzeRequestDto;
import com.kh.investSpring.domain.ai.dto.UserProfileAnalyzeResponseDto;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.user.dao.UserDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAiProfileServiceImpl implements UserAiProfileService {

    private final UserAiProfileDao userAiProfileDao;
    private final UserDao userDao;
    private final AccountDao accountDao;
    private final StockDao stockDao;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    @Value("${ai.base.url}")
    private String aiUrl;

    @Override
    public UserAiProfileDto getUserProfile(Long userNo) {
        UserAiProfileDto dto = userAiProfileDao.findByUserNo(userNo);

        if (dto == null) {
            return null;
        }

        try {
        	String riskJson = dto.getPortfolioRiskAnalysisJson();
        	String recommendationJson = dto.getAiRecommendationsJson();

        	List<String> risks = Collections.emptyList();
        	List<String> recommendations = Collections.emptyList();

        	if (riskJson != null && !riskJson.isBlank()) {
        	    risks = objectMapper.readValue(
        	        riskJson,
        	        new TypeReference<List<String>>() {}
        	    );
        	}

        	if (recommendationJson != null
        	        && !recommendationJson.isBlank()) {

        	    recommendations = objectMapper.readValue(
        	        recommendationJson,
        	        new TypeReference<List<String>>() {}
        	    );
        	}

        	dto.setPortfolioRiskAnalysis(risks);
        	dto.setAiRecommendations(recommendations);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dto;
    }
    
    @Override
    public void analyzeUserProfile(Long userNo) {
        try {
            /*
             * 1. 설문 결과 조회
             */
            InvestmentTypeDto investmentType = userDao.selectInvestmentByUserNo(userNo);

            if (investmentType == null) {
                throw new IllegalArgumentException(
                    "투자 성향 설문 결과가 없습니다."
                );
            }

            /*
             * 2. 설문 JSON 읽기
             */
            String resultFile = investmentType.getResultFile();
            Path path = Paths.get(resultFile);
            ObjectMapper mapper = objectMapper;
            JsonNode root = mapper.readTree(path.toFile());
            String surveyResult = root.path("resultType").asText();

            List<UserProfileAnalyzeRequestDto.SurveyAnswerDto>
                surveyAnswers = new ArrayList<>();

            JsonNode answers = root.path("answers");

            for (JsonNode answer : answers) {
                UserProfileAnalyzeRequestDto.SurveyAnswerDto dto =
                    new UserProfileAnalyzeRequestDto.SurveyAnswerDto();

                dto.setQuestion(answer.path("questionText").asText());
                dto.setAnswer(answer.path("optionText").asText());
                surveyAnswers.add(dto);
            }

            /*
             * 3. 계좌 조회
             */
            AccountAssetSummaryDto account = accountDao.selectAccountAssetByUserNo(userNo);

            if (account == null) {
                throw new IllegalArgumentException(
                    "계좌 정보가 없습니다."
                );
            }

            /*
             * 4. 보유 종목 조회
             */
            List<HoldingStock> holdings = accountDao.selectHoldingStocksByUserNo(userNo);

            if (holdings == null) {
                holdings = new ArrayList<>();
            }

            /*
             * 5. 총 자산 계산
             */
            long totalStockValue = 0L;
            Map<String, Long> sectorAmountMap = new HashMap<>();
            Map<String, Long> marketAmountMap = new HashMap<>();

            for (HoldingStock holding : holdings) {
                StockDto stock = stockDao.findByStockCode(holding.getStockCode());

                if (stock == null) {
                    continue;
                }

                long stockValue = stock.getPrice() * holding.getQuantity();

                totalStockValue += stockValue;

                /*
                 * sector
                 */
                String sector = stock.getSector();

                sectorAmountMap.put(
                    sector,
                    sectorAmountMap.getOrDefault(
                        sector,
                        0L
                    ) + stockValue
                );

                /*
                 * market
                 */
                String marketType = stock.getMarketType();

                marketAmountMap.put(
                    marketType,
                    marketAmountMap.getOrDefault(
                        marketType,
                        0L
                    ) + stockValue
                );
            }

            BigDecimal totalAsset = account.getAvailableCash().add(BigDecimal.valueOf(totalStockValue));

            /*
             * 6. 현금 비중 계산
             */
            double cashRatio = 0.0;

            if (totalAsset.compareTo(BigDecimal.ZERO) > 0) {
                cashRatio = account.getAvailableCash()
                					.divide(
                							totalAsset,
                							4,
                							RoundingMode.HALF_UP
                							)
                					.multiply(BigDecimal.valueOf(100))
                					.doubleValue();
            }

            /*
             * 7. sector allocation 계산
             */
            List<UserProfileAnalyzeRequestDto.SectorAllocationDto>
                sectorAllocations = new ArrayList<>();

            for (Map.Entry<String, Long> entry : sectorAmountMap.entrySet()) {
                int ratio = 0;
                
                if (totalStockValue > 0) {
                    ratio = (int) Math.round(
                            ((double) entry.getValue() / totalStockValue) * 100.0
                        );
                }

                UserProfileAnalyzeRequestDto
                    .SectorAllocationDto dto =
                        new UserProfileAnalyzeRequestDto
                            .SectorAllocationDto();

                dto.setSector(entry.getKey());
                dto.setRatio(ratio);
                sectorAllocations.add(dto);
            }

            /*
             * 8. market allocation 계산
             */
            Map<String, Integer> marketAllocation = new HashMap<>();
            marketAllocation.put("KOSPI", 0);
            marketAllocation.put("KOSDAQ", 0);

            for (Map.Entry<String, Long> entry : marketAmountMap.entrySet()) {

                int ratio = 0;

                if (totalStockValue > 0) {
                    ratio = (int) Math.round(
                        ((double) entry.getValue() / totalStockValue) * 100.0
                    );
                }

                marketAllocation.put(entry.getKey(), ratio);
            }

            /*
             * 9. FastAPI Request 생성
             */
            UserProfileAnalyzeRequestDto requestDto = new UserProfileAnalyzeRequestDto();
            requestDto.setSurveyResult(surveyResult);
            requestDto.setSurveyAnswers(surveyAnswers);

            UserProfileAnalyzeRequestDto.PortfolioDto 
            portfolioDto = new UserProfileAnalyzeRequestDto.PortfolioDto();
            portfolioDto.setTotalAsset(totalAsset);
            portfolioDto.setCashRatio(cashRatio);
            portfolioDto.setStockCount(holdings.size());
            portfolioDto.setSectorAllocation(sectorAllocations);
            portfolioDto.setMarketAllocation(marketAllocation);
            requestDto.setPortfolio(portfolioDto);

            /*
             * 10. FastAPI 호출
             */
            UserProfileAnalyzeResponseDto response =
                restTemplate.postForObject(aiUrl + "/analysis/user-profile",
                    requestDto,
                    UserProfileAnalyzeResponseDto.class
                );

            if (response == null) {
                throw new RuntimeException("AI 응답이 없습니다.");
            }

            /*
             * 11. JSON 변환
             */
            String riskJson = mapper.writeValueAsString(response.getPortfolioRiskAnalysis());
            String recommendationJson = mapper.writeValueAsString(response.getAiRecommendations());

            /*
             * 12. 저장 DTO 생성
             */
            UserAiProfileDto saveDto = new UserAiProfileDto();

            saveDto.setUserNo(userNo);
            saveDto.setSurveyType(response.getSurveyType());
            saveDto.setActualInvestmentType(response.getActualInvestmentType());
            saveDto.setPortfolioRiskAnalysisJson(riskJson);
            saveDto.setAiRecommendationsJson(recommendationJson);

            /*
             * 13. upsert
             */
            if (userAiProfileDao.exists(userNo)) {
                userAiProfileDao.update(saveDto);
            } else {
                userAiProfileDao.insert(saveDto);
            }

        } catch (Exception e) {
            log.error("유저 AI 프로필 분석 실패 userNo={}",
                userNo,
                e
            );

            throw new RuntimeException(e);
        }
    }
}