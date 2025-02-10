package com.tenacy.snaplink.config;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import com.tenacy.snaplink.util.Base62Encoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("test") // 테스트 프로필에서만 이 컴포넌트가 활성화됨
public class LoadTestDataGenerator {
    private final UrlRepository urlRepository;
    private final Base62Encoder encoder;

    private static final String TEST_DATA_DIR = "test-data";
    private static final String CSV_FILE_NAME = "short-codes.csv";
    private static final int TEST_DATA_COUNT = 1000;

    @PostConstruct
    @Transactional
    public void generateTestData() {
        // 디렉토리와 파일 경로 설정
        Path dirPath = Paths.get(TEST_DATA_DIR);
        Path filePath = dirPath.resolve(CSV_FILE_NAME);

        // 테스트 디렉토리가 없으면 생성
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectory(dirPath);
                log.info("Created test data directory: {}", dirPath.toAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to create test data directory", e);
                return;
            }
        }

        List<Url> urls = new ArrayList<>();
        Map<String, String> codeToUrlMap = new HashMap<>();

        // 파일이 존재하는 경우 파일에서 코드 읽어오기
        if (Files.exists(filePath)) {
            log.info("Test data file exists. Loading data from: {}", filePath.toAbsolutePath());
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String shortCode = parts[0].trim();
                        String originalUrl = parts[1].trim();
                        codeToUrlMap.put(shortCode, originalUrl);
                    } else if (parts.length == 1) {
                        // 이전 포맷 지원 (shortCode만 있는 경우)
                        String shortCode = parts[0].trim();
                        codeToUrlMap.put(shortCode, "https://example.com/test-" + shortCode);
                    }
                }
                log.info("Loaded {} short codes from existing file", codeToUrlMap.size());
            } catch (IOException e) {
                log.error("Error reading existing test data file", e);
                // 파일 읽기 실패시 새로 생성
                codeToUrlMap.clear();
            }
        }

        // 기존 파일에서 읽어온 데이터가 있으면 사용, 없으면 새로 생성
        if (!codeToUrlMap.isEmpty()) {
            for (Map.Entry<String, String> entry : codeToUrlMap.entrySet()) {
                Url url = new Url();
                url.setShortCode(entry.getKey());
                url.setOriginalUrl(entry.getValue());
                url.setCreatedAt(LocalDateTime.now());
                url.setClickCount(0L);
                urls.add(url);
            }
        } else {
            log.info("Generating new test data...");
            // 새로운 테스트 데이터 생성
            for (int i = 0; i < TEST_DATA_COUNT; i++) {
                String originalUrl = "https://example.com/test-" + i;
                String shortCode = encoder.encode(1000 + i);

                Url url = new Url();
                url.setOriginalUrl(originalUrl);
                url.setShortCode(shortCode);
                url.setCreatedAt(LocalDateTime.now());
                url.setClickCount(0L);

                urls.add(url);
                codeToUrlMap.put(shortCode, originalUrl);
            }

            // CSV 파일로 저장 (JMeter에서 사용)
            try (PrintWriter writer = new PrintWriter(new File(filePath.toString()))) {
                writer.println("shortCode,originalUrl");
                for (Map.Entry<String, String> entry : codeToUrlMap.entrySet()) {
                    writer.println(entry.getKey() + "," + entry.getValue());
                }
                log.info("Generated new test data CSV file: {}", filePath.toAbsolutePath());
            } catch (FileNotFoundException e) {
                log.error("Failed to create test data CSV file", e);
            }
        }

        // 기존 테스트 데이터 삭제 후 새로 저장
        urlRepository.deleteAll(); // 기존 데이터 삭제
        urlRepository.saveAll(urls);
        log.info("Saved {} test URLs to database", urls.size());
    }
}