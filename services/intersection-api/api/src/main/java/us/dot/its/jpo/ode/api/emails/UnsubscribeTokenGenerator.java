package us.dot.its.jpo.ode.api.emails;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;

import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generator for creating and validating JWT-based unsubscribe tokens for email
 * notifications.
 * 
 * <p>
 * This component generates signed JWT tokens that are used to authenticate
 * users when they
 * click unsubscribe links in emails. The tokens are signed using HMAC-SHA256
 * and include
 * the user's email address as the subject, along with validation claims for
 * issuer and purpose.
 * </p>
 * 
 * <p>
 * Tokens do not expire by default, allowing users to unsubscribe at any time
 * using the
 * link provided in their emails.
 * </p>
 */
@Slf4j
@Component
public class UnsubscribeTokenGenerator {
    private final EmailProperties emailProperties;
    private final String kcIssuerUri;

    public UnsubscribeTokenGenerator(EmailProperties emailProperties,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String kcIssuerUri) {
        this.emailProperties = emailProperties;
        this.kcIssuerUri = kcIssuerUri;
    }

    /**
     * Generates a complete unsubscribe URL with an embedded JWT token.
     * 
     * <p>
     * This method creates a signed JWT token for the given email address and embeds
     * it
     * in a URL-encoded query parameter. The resulting URL can be used directly in
     * email
     * notifications to allow users to manage their subscription preferences.
     * </p>
     *
     * @param emailAddress The email address of the user for whom to generate the
     *                     unsubscribe URL.
     * @return A complete URL string pointing to the unsubscribe page with the token
     *         parameter,
     *         or null if token generation fails.
     */
    public String generateUnsubscribeUrl(String emailAddress) {
        String token = generateUnsubscribeToken(emailAddress);

        if (token == null) {
            return null; // Token generation failed
        }

        // Encode the token to ensure it is URL-safe
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

        // Build the unsubscribe URL
        return String.format("%s/unsubscribe?token=%s", emailProperties.getCvmgrFrontEndUri(), encodedToken);
    }

    /**
     * Generates a signed JWT token for email unsubscribe authentication.
     * 
     * <p>
     * The token is signed using HMAC-SHA256 (HS256) and contains the following
     * claims:
     * </p>
     * <ul>
     * <li><b>issuer</b>: The Keycloak issuer URI from application
     * configuration</li>
     * <li><b>subject</b>: The email address of the user</li>
     * <li><b>purpose</b>: Set to "unsubscribe" to identify the token's intended
     * use</li>
     * <li><b>issueTime</b>: The current date/time when the token was created</li>
     * </ul>
     * 
     * <p>
     * Note: This token does not include an expiration time, so it remains valid
     * indefinitely.
     * </p>
     *
     * @param email The email address of the user to include as the token's subject.
     * @return The serialized JWT token as a String, or null if signing fails due to
     *         an invalid
     *         secret key or other JOSE exception.
     */
    public String generateUnsubscribeToken(String email) {
        // Create the JWT claims
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(kcIssuerUri) // Set the issuer
                .subject(email) // Set the subject (email address)
                .claim("purpose", "unsubscribe") // Add the purpose claim
                .issueTime(new Date()) // Set the issue time
                .build();

        // Create the HMAC signer with the secret key
        JWSSigner signer;
        try {
            signer = new MACSigner(emailProperties.getUnsubscribeSecretKey());
        } catch (KeyLengthException e) {
            log.error("Invalid key length for unsubscribe secret key", e);
            return null;
        }

        // Create the signed JWT
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256), // Specify the signing algorithm
                claimsSet);

        // Sign the JWT
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            log.error("Error signing the JWT for unsubscribe token", e);
            return null;
        }

        // Return the serialized token
        return signedJWT.serialize();
    }

    /**
     * Parses and validates an unsubscribe JWT token, returning the email address if
     * valid.
     * 
     * <p>
     * This method performs the following validations:
     * </p>
     * <ol>
     * <li>Parses the JWT token structure</li>
     * <li>Verifies the HMAC-SHA256 signature using the configured secret key</li>
     * <li>Checks if the token has expired (if an expiration time is present)</li>
     * <li>Validates that the issuer matches the configured Keycloak issuer URI</li>
     * <li>Confirms the "purpose" claim is set to "unsubscribe"</li>
     * </ol>
     * 
     * <p>
     * If any validation fails or an exception occurs during parsing, this method
     * returns null.
     * </p>
     *
     * @param token The JWT token string to parse and validate.
     * @return The email address from the token's subject claim if all validations
     *         pass,
     *         or null if the token is invalid, expired, or cannot be parsed.
     */
    public String parseAndValidateToken(String token) {
        try {
            // Parse the JWT token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Create a verifier with the secret key
            JWSVerifier verifier = new MACVerifier(emailProperties.getUnsubscribeSecretKey());

            // Verify the signature
            if (!signedJWT.verify(verifier)) {
                return null; // Signature verification failed
            }

            // Get the claims set
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // Validate the claims (e.g., check expiration, issuer, purpose)
            Date now = new Date();
            if (claimsSet.getExpirationTime() != null && claimsSet.getExpirationTime().before(now)) {
                return null; // Token is expired
            }
            if (!claimsSet.getIssuer().equals(kcIssuerUri)) {
                return null; // Invalid issuer
            }
            if (!"unsubscribe".equals(claimsSet.getStringClaim("purpose"))) {
                return null; // Invalid purpose
            }

            // Return the subject (email address) if all validations pass
            return claimsSet.getSubject();
        } catch (Exception e) {
            return null; // Exception occurred, return null
        }
    }
}