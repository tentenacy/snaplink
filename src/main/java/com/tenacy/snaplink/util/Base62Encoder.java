package com.tenacy.snaplink.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Base62Encoder {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();
    private static final Random RANDOM = new Random();

    // ID를 Base62로 인코딩
    public String encode(long id) {
        StringBuilder sb = new StringBuilder();

        if (id == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }

        // 결과가 항상 7자리가 되도록 패딩
        while (sb.length() < 7) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(BASE)));
        }

        // 순서 뒤집기
        return sb.reverse().toString();
    }

    // 무작위 7자리 코드 생성
    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(BASE)));
        }
        return sb.toString();
    }
}