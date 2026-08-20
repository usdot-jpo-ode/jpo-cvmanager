package us.dot.its.jpo.ode.api.emails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
public class UnsubscribeTokenGeneratorTest {

    @Mock
    private EmailProperties emailProperties;

    @InjectMocks
    private UnsubscribeTokenGenerator tokenGenerator;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String SECRET_KEY = "this-is-a-very-secure-secret-key-for-testing-purposes-123456";
    private static final String ISSUER_URI = "http://localhost:8080/realms/test";
    private static final String FRONTEND_URI = "http://localhost:3000";

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(emailProperties.getUnsubscribeSecretKey()).thenReturn(SECRET_KEY);
        lenient().when(emailProperties.getCvmgrFrontEndUri()).thenReturn(FRONTEND_URI);

        // Use reflection to set the kcIssuerUri field
        Field issuerField = UnsubscribeTokenGenerator.class.getDeclaredField("kcIssuerUri");
        issuerField.setAccessible(true);
        issuerField.set(tokenGenerator, ISSUER_URI);
    }

    @Test
    void testGenerateUnsubscribeToken_ValidEmail() {
        // Act
        String token = tokenGenerator.generateUnsubscribeToken(TEST_EMAIL);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify it's a valid JWT format (three parts separated by dots)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have three parts");
    }

    @Test
    void testGenerateUnsubscribeToken_DifferentEmails_ProduceDifferentTokens() {
        // Act
        String token1 = tokenGenerator.generateUnsubscribeToken("user1@example.com");
        String token2 = tokenGenerator.generateUnsubscribeToken("user2@example.com");

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(!token1.equals(token2), "Different emails should produce different tokens");
    }

    @Test
    void testGenerateUnsubscribeUrl_ValidEmail() {
        // Act
        String url = tokenGenerator.generateUnsubscribeUrl(TEST_EMAIL);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith(FRONTEND_URI + "/unsubscribe?token="));

        // Extract and verify the token parameter is URL-encoded
        String tokenPart = url.substring((FRONTEND_URI + "/unsubscribe?token=").length());
        assertNotNull(tokenPart);
        assertTrue(tokenPart.length() > 0);
    }

    @Test
    void testGenerateUnsubscribeUrl_ContainsValidToken() throws Exception {
        // Act
        String url = tokenGenerator.generateUnsubscribeUrl(TEST_EMAIL);

        // Assert
        String tokenPart = url.substring((FRONTEND_URI + "/unsubscribe?token=").length());
        String decodedToken = URLDecoder.decode(tokenPart, StandardCharsets.UTF_8);

        // Parse the JWT to verify it's valid
        SignedJWT signedJWT = SignedJWT.parse(decodedToken);
        assertNotNull(signedJWT);
        assertEquals(TEST_EMAIL, signedJWT.getJWTClaimsSet().getSubject());
    }

    @Test
    void testParseAndValidateToken_ValidToken() {
        // Arrange
        String token = tokenGenerator.generateUnsubscribeToken(TEST_EMAIL);

        // Act
        String email = tokenGenerator.parseAndValidateToken(token);

        // Assert
        assertNotNull(email);
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testParseAndValidateToken_InvalidSignature() throws Exception {
        // Arrange - Create a token with a different secret key
        String differentSecretKey = "a-completely-different-secret-key-that-wont-match-the-original";
        JWSSigner signer = new MACSigner(differentSecretKey);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                .claim("purpose", "unsubscribe")
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String invalidToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(invalidToken);

        // Assert
        assertNull(email, "Token with invalid signature should return null");
    }

    @Test
    void testParseAndValidateToken_ExpiredToken() throws Exception {
        // Arrange - Create a token that expired 1 hour ago
        JWSSigner signer = new MACSigner(SECRET_KEY);

        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                .claim("purpose", "unsubscribe")
                .issueTime(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expirationTime(oneHourAgo)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String expiredToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(expiredToken);

        // Assert
        assertNull(email, "Expired token should return null");
    }

    @Test
    void testParseAndValidateToken_WrongIssuer() throws Exception {
        // Arrange - Create a token with wrong issuer
        JWSSigner signer = new MACSigner(SECRET_KEY);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("http://wrong-issuer.com")
                .subject(TEST_EMAIL)
                .claim("purpose", "unsubscribe")
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String invalidToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(invalidToken);

        // Assert
        assertNull(email, "Token with wrong issuer should return null");
    }

    @Test
    void testParseAndValidateToken_WrongPurpose() throws Exception {
        // Arrange - Create a token with wrong purpose
        JWSSigner signer = new MACSigner(SECRET_KEY);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                .claim("purpose", "password-reset") // Wrong purpose
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String invalidToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(invalidToken);

        // Assert
        assertNull(email, "Token with wrong purpose should return null");
    }

    @Test
    void testParseAndValidateToken_MissingPurpose() throws Exception {
        // Arrange - Create a token without purpose claim
        JWSSigner signer = new MACSigner(SECRET_KEY);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                // No purpose claim
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String invalidToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(invalidToken);

        // Assert
        assertNull(email, "Token without purpose claim should return null");
    }

    @Test
    void testParseAndValidateToken_MalformedToken() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt";

        // Act
        String email = tokenGenerator.parseAndValidateToken(malformedToken);

        // Assert
        assertNull(email, "Malformed token should return null");
    }

    @Test
    void testParseAndValidateToken_EmptyToken() {
        // Act
        String email = tokenGenerator.parseAndValidateToken("");

        // Assert
        assertNull(email, "Empty token should return null");
    }

    @Test
    void testParseAndValidateToken_NullToken() {
        // Act
        String email = tokenGenerator.parseAndValidateToken(null);

        // Assert
        assertNull(email, "Null token should return null");
    }

    @Test
    void testParseAndValidateToken_ValidTokenWithoutExpiration() throws Exception {
        // Arrange - Create a valid token without expiration time (should be valid)
        JWSSigner signer = new MACSigner(SECRET_KEY);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                .claim("purpose", "unsubscribe")
                .issueTime(new Date())
                // No expiration time
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String validToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(validToken);

        // Assert
        assertNotNull(email, "Token without expiration should be valid");
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testParseAndValidateToken_ValidTokenWithFutureExpiration() throws Exception {
        // Arrange - Create a token that expires in 1 hour
        JWSSigner signer = new MACSigner(SECRET_KEY);

        Date oneHourFromNow = new Date(System.currentTimeMillis() + 3600000);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(TEST_EMAIL)
                .claim("purpose", "unsubscribe")
                .issueTime(new Date())
                .expirationTime(oneHourFromNow)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);
        String validToken = signedJWT.serialize();

        // Act
        String email = tokenGenerator.parseAndValidateToken(validToken);

        // Assert
        assertNotNull(email, "Token with future expiration should be valid");
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testGenerateUnsubscribeToken_TooShortSecretKey() throws Exception {
        // Arrange - Mock a secret key that's too short (less than 32 bytes for HS256)
        when(emailProperties.getUnsubscribeSecretKey()).thenReturn("short");

        // Act
        String token = tokenGenerator.generateUnsubscribeToken(TEST_EMAIL);

        // Assert
        assertNull(token, "Should return null when secret key is too short");
    }

    @Test
    void testRoundTrip_GenerateAndValidate() {
        // Act - Generate a token and then validate it
        String generatedToken = tokenGenerator.generateUnsubscribeToken(TEST_EMAIL);
        String extractedEmail = tokenGenerator.parseAndValidateToken(generatedToken);

        // Assert
        assertNotNull(generatedToken);
        assertNotNull(extractedEmail);
        assertEquals(TEST_EMAIL, extractedEmail);
    }

    @Test
    void testGenerateUnsubscribeUrl_SpecialCharactersInEmail() {
        // Arrange
        String emailWithSpecialChars = "test+tag@example.com";

        // Act
        String url = tokenGenerator.generateUnsubscribeUrl(emailWithSpecialChars);
        String tokenPart = url.substring((FRONTEND_URI + "/unsubscribe?token=").length());
        String decodedToken = URLDecoder.decode(tokenPart, StandardCharsets.UTF_8);
        String extractedEmail = tokenGenerator.parseAndValidateToken(decodedToken);

        // Assert
        assertNotNull(url);
        assertNotNull(extractedEmail);
        assertEquals(emailWithSpecialChars, extractedEmail);
    }
}
