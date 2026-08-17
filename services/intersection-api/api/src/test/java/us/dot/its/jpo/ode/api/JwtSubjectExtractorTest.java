package us.dot.its.jpo.ode.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@ExtendWith(MockitoExtension.class)
class JwtSubjectExtractorTest {

    @Test
    void extractSubject_nullHeader_returnsNull() {
        JwtSubjectExtractor extractor = new JwtSubjectExtractor(null);
        assertNull(extractor.extractSubject(null));
    }

    @Test
    void extractSubject_blankHeader_returnsNull() {
        JwtSubjectExtractor extractor = new JwtSubjectExtractor(null);
        assertNull(extractor.extractSubject("   "));
    }

    @Test
    void extractSubject_missingTokenPart_returnsNull() {
        JwtSubjectExtractor extractor = new JwtSubjectExtractor(null);
        assertNull(extractor.extractSubject("Bearer"));
    }

    @Test
    void extractSubject_withJwtDecoder_decodedSubjectReturned() {
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode("tokenA")).thenReturn(Jwt.withTokenValue("tokenA")
                .headers(h -> h.put("alg", "none"))
                .claims(c -> c.put("sub", "decoded-sub"))
                .build());

        JwtSubjectExtractor extractor = new JwtSubjectExtractor(decoder);
        assertEquals("decoded-sub", extractor.extractSubject("Bearer tokenA"));
    }

    @Test
    void extractSubject_withJwtDecoderThrowing_returnsNull() {
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode("tokenX")).thenThrow(new RuntimeException("bad token"));

        JwtSubjectExtractor extractor = new JwtSubjectExtractor(decoder);
        assertNull(extractor.extractSubject("Bearer tokenX"));
    }

    @Test
    void extractSubject_fallbackBase64Payload_returnsSub() {
        // Build a JWT-like token with a base64url payload containing {"sub":"payload-sub"}
        String headerJson  = "{\"alg\":\"none\"}";
        String payloadJson = "{\"sub\":\"payload-sub\"}";
        String header  = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String token = header + "." + payload + ".sig";

        JwtSubjectExtractor extractor = new JwtSubjectExtractor(null);
        assertEquals("payload-sub", extractor.extractSubject("Bearer " + token));
    }

    @Test
    void extractSubject_fallbackBase64_invalidPayload_returnsNull() {
        JwtSubjectExtractor extractor = new JwtSubjectExtractor(null);
        // Only two parts but the payload is not valid base64
        assertNull(extractor.extractSubject("Bearer header.!!!.sig"));
    }
}
