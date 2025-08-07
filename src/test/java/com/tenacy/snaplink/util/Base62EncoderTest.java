package com.tenacy.snaplink.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class Base62EncoderTest {
    @Autowired
    private Base62Encoder encoder;

    @Test
    public void testEncode() {
        String encoded = encoder.encode(12345);
        assertNotNull(encoded);
        assertEquals(7, encoded.length());
    }

    @Test
    public void testRandomCode() {
        String code1 = encoder.generateRandomCode();
        String code2 = encoder.generateRandomCode();

        assertNotEquals(code1, code2);
        assertEquals(7, code1.length());
        assertEquals(7, code2.length());
    }
}