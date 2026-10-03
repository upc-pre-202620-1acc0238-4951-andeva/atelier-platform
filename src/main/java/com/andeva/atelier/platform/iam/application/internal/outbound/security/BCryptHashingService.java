package com.andeva.atelier.platform.iam.application.internal.outbound.security;

/**
 * Outbound port for cryptographic password hashing and verification using the BCrypt algorithm.
 *
 * @author Joel Huamani Estefanero
 */
public interface BCryptHashingService {

    /**
     * Hashes a raw plaintext password using BCrypt with a minimum work factor of 12.
     *
     * @param rawPassword the plaintext password
     * @return the encoded BCrypt hash string
     */
    String hash(String rawPassword);

    /**
     * Checks whether a raw plaintext password matches a stored BCrypt hash.
     *
     * @param rawPassword the plaintext password to verify
     * @param encodedHash the stored BCrypt hash
     * @return true if the credentials match, false otherwise
     */
    boolean matches(String rawPassword, String encodedHash);
}
