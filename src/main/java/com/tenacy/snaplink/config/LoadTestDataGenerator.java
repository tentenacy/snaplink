package com.tenacy.snaplink.config;

import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import com.tenacy.snaplink.util.Base62Encoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("test") // 테스트 프로필에서만 이 컴포넌트가 활성화됨
public class LoadTestDataGenerator {
    private final UrlRepository urlRepository;
    private final Base62Encoder encoder;

    @PostConstruct
    @Transactional
    public void generateTestData() {
        List<String> shortCodes = new ArrayList<>();

        // 1000개의 테스트 URL 생성
        for (int i = 0; i < 1000; i++) {
            Url url = new Url();
            url.setOriginalUrl("https://example.com/test-" + i);
            url.setShortCode(encoder.encode(1000 + i));
            url.setCreatedAt(LocalDateTime.now());
            url.setClickCount(0L);

            urlRepository.save(url);
            shortCodes.add(url.getShortCode());
        }

        // CSV 파일로 저장 (JMeter에서 사용)
        try (PrintWriter writer = new PrintWriter(new File("test-data/short-codes.csv"))) {
            writer.println("shortCode");
            for (String code : shortCodes) {
                writer.println(code);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}