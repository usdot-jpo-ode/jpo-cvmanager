package us.dot.its.jpo.ode.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Extracts the "sub" (subject) claim from a JWT Authorization header value.
 *
 * <p>Intended for Bucket4j rate-limiting cache keys. Call from SpEL as:
 * <pre>
 *   @jwtSubjectExtractor.extractSubject(getHeader('Authorization'))
 * </pre>
 * where {@code getHeader} is invoked on the root {@link jakarta.servlet.http.HttpServletRequest}
 * that Bucket4j places in the SpEL evaluation context.
 *
 * <p>When a {@link JwtDecoder} bean is present it is used for decoding (allowing
 * tests to control the returned subject via mocks). Otherwise the payload is
 * decoded from base64 directly without signature verification.
 */
@Component("jwtSubjectExtractor")
public class JwtSubjectExtractor {

    private final JwtDecoder jwtDecoder;

    public JwtSubjectExtractor(@Nullable JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Extracts the JWT {@code sub} claim from a raw {@code Authorization} header value.
     *
     * @param authorizationHeader the full header value, e.g. {@code "Bearer eyJ..."}
     * @return the subject string, or {@code null} if it cannot be determined
     */
    public String extractSubject(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        // Expect "Bearer <token>"
        String[] parts = authorizationHeader.split(" ");
        if (parts.length < 2) {
            return null;
        }
        String token = parts[parts.length - 1].trim();

        // If a JwtDecoder bean is present (tests mock it), use it so tests can
        // control the decoded subject. Otherwise fall back to a lightweight
        // base64 decode of the JWT payload.
        if (jwtDecoder != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                Object sub = jwt.getClaims().get("sub");
                return sub == null ? null : sub.toString();
            } catch (Exception e) {
                return null;
            }
        }

        // Fallback: expect a three-part JWT separated by literal dots
        String[] tokenParts = token.split("\\.");
        if (tokenParts.length < 2) {
            return null;
        }
        String payload = tokenParts[1];
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded, StandardCharsets.UTF_8);
            // naive extraction of "sub" value to avoid adding a JSON dependency
            String key = "\"sub\"";
            int idx = json.indexOf(key);
            if (idx < 0) {
                return null;
            }
            int colon = json.indexOf(':', idx + key.length());
            if (colon < 0) {
                return null;
            }
            int start = json.indexOf('"', colon);
            if (start < 0) {
                return null;
            }
            int end = json.indexOf('"', start + 1);
            if (end < 0) {
                return null;
            }
            String sub = json.substring(start + 1, end);
            return sub;
        } catch (IllegalArgumentException e) {
            // invalid base64
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
