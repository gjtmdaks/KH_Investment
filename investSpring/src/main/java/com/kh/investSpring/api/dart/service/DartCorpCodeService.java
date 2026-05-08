package com.kh.investSpring.api.dart.service;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.kh.investSpring.api.dart.config.DartProperties;
import com.kh.investSpring.api.dart.dao.corpInformationDao;
import com.kh.investSpring.api.dart.dto.DartCorpCodeDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DartCorpCodeService {

    private final DartProperties properties;
    private final corpInformationDao CoDao;

    public String downloadCorpCodeXml() {

        try {
            String url =
                    "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key="
                    + properties.getAppKey();

            HttpClient client =
                    HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .build();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            byte[] zipBytes = response.body();

            if (zipBytes == null || zipBytes.length == 0) {
                throw new RuntimeException("ZIP 다운로드 실패");
            }

            ZipInputStream zis =
                    new ZipInputStream(
                            new ByteArrayInputStream(zipBytes)
                    );

            ZipEntry entry = zis.getNextEntry();

            if (entry == null) {
                throw new RuntimeException("ZIP 내부 XML 없음");
            }

            String xml =
                    new String(
                            zis.readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            zis.close();

            log.info("corpCode XML 다운로드 완료");

            return xml;

        } catch (Exception e) {

            log.error("corpCode 다운로드 실패", e);

            throw new RuntimeException(e);
        }
    }
    
    public List<DartCorpCodeDto> parseXml(String xml) {

        List<DartCorpCodeDto> list = new ArrayList<>();

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(
                            new InputSource(
                                    new StringReader(xml)
                            )
                    );

            NodeList nodeList =
                    document.getElementsByTagName("list");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                String corpCode =
                        getTagValue(element, "corp_code");

                String stockCode =
                        getTagValue(element, "stock_code");

                String corpName =
                        getTagValue(element, "corp_name");

                if (stockCode == null || stockCode.isBlank()) {
                    continue;
                }

                DartCorpCodeDto dto =
                        new DartCorpCodeDto();

                dto.setCorpCode(corpCode);
                dto.setStockCode(stockCode);
                dto.setCorpName(corpName);

                list.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
    
    private String getTagValue(
            Element element,
            String tagName
    ) {

        NodeList list =
                element.getElementsByTagName(tagName);

        if (list.getLength() == 0) {
            return null;
        }

        return list.item(0).getTextContent();
    }
    
    @Transactional
    public void syncCorpCodes() {

        // 1. XML 다운로드
        String xml = downloadCorpCodeXml();

        // 2. XML → DTO List
        List<DartCorpCodeDto> list =
                parseXml(xml);

        log.info("corpCode 저장 시작 count={}", list.size());

        // 3. DB 저장
        for (DartCorpCodeDto dto : list) {
        	CoDao.mergeCorpCode(dto);
        }

        log.info("corpCode 저장 완료");
    }
}