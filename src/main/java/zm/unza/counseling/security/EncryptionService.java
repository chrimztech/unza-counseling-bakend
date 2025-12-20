package zm.unza.counseling.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Enterprise-grade encryption service for data protection and privacy compliance.
 * Provides AES-256 encryption for sensitive data at rest and in transit.
 */
@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_LENGTH = 256;
    
    private static final Logger logger = Logger.getLogger(EncryptionService.class.getName());
    
    private final SecretKey secretKey;
    
    public EncryptionService() {
        try {
            this.secretKey = generateSecretKey();
        } catch (Exception e) {
            logger.severe("Failed to initialize encryption service: " + e.getMessage());
            throw new RuntimeException("Encryption service initialization failed", e);
        }
    }
    
    /**
     * Encrypt sensitive data using AES-256 encryption.
     * 
     * @param data The data to encrypt
     * @return Base64 encoded encrypted data
     * @throws Exception if encryption fails
     */
    public String encrypt(String data) throws Exception {
        if (data == null || data.trim().isEmpty()) {
            return data;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            
            String encryptedString = Base64.getEncoder().encodeToString(encryptedData);
            logger.info("Data encrypted successfully");
            
            return encryptedString;
        } catch (Exception e) {
            logger.severe("Encryption failed: " + e.getMessage());
            throw new Exception("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt encrypted data.
     * 
     * @param encryptedData Base64 encoded encrypted data
     * @return Decrypted data
     * @throws Exception if decryption fails
     */
    public String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            return encryptedData;
        }
        
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedData = cipher.doFinal(decodedData);
            
            String decryptedString = new String(decryptedData);
            logger.info("Data decrypted successfully");
            
            return decryptedString;
        } catch (Exception e) {
            logger.severe("Decryption failed: " + e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }
    
    /**
     * Hash sensitive data using SHA-256 for one-way encryption.
     * 
     * @param data The data to hash
     * @return SHA-256 hash of the data
     * @throws Exception if hashing fails
     */
    public String hash(String data) throws Exception {
        if (data == null || data.trim().isEmpty()) {
            return data;
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            
            String hashedString = Base64.getEncoder().encodeToString(hash);
            logger.info("Data hashed successfully");
            
            return hashedString;
        } catch (Exception e) {
            logger.severe("Hashing failed: " + e.getMessage());
            throw new Exception("Hashing failed", e);
        }
    }
    
    /**
     * Generate a cryptographically secure random key.
     * 
     * @return Base64 encoded random key
     * @throws Exception if key generation fails
     */
    public String generateRandomKey() throws Exception {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey randomKey = keyGenerator.generateKey();
            
            String keyString = Base64.getEncoder().encodeToString(randomKey.getEncoded());
            logger.info("Random key generated successfully");
            
            return keyString;
        } catch (Exception e) {
            logger.severe("Random key generation failed: " + e.getMessage());
            throw new Exception("Random key generation failed", e);
        }
    }
    
    /**
     * Validate password strength for enterprise compliance.
     * 
     * @param password The password to validate
     * @return Validation result with detailed feedback
     */
    public PasswordValidationResult validatePasswordStrength(String password) {
        if (password == null) {
            return new PasswordValidationResult(false, "Password cannot be null");
        }
        
        if (password.length() < 12) {
            return new PasswordValidationResult(false, "Password must be at least 12 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            return new PasswordValidationResult(false, "Password must contain at least one digit");
        }
        
        if (!password.matches(".*[@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one special character");
        }
        
        return new PasswordValidationResult(true, "Password meets all enterprise security requirements");
    }
    
    /**
     * Generate secure hash for password storage.
     * 
     * @param password The password to hash
     * @param salt The salt to use for hashing
     * @return Secure hash for password storage
     * @throws Exception if hashing fails
     */
    public String generateSecurePasswordHash(String password, String salt) throws Exception {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        
        try {
            String saltedPassword = password + salt;
            return hash(saltedPassword);
        } catch (Exception e) {
            logger.severe("Secure password hash generation failed: " + e.getMessage());
            throw new Exception("Secure password hash generation failed", e);
        }
    }
    
    /**
     * Generate cryptographically secure random salt for password hashing.
     * 
     * @return Base64 encoded random salt
     * @throws Exception if salt generation fails
     */
    public String generateSalt() throws Exception {
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Generate a secret key for encryption.
     * 
     * @return SecretKey for AES encryption
     * @throws Exception if key generation fails
     */
    private SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_LENGTH);
        return keyGenerator.generateKey();
    }
    
    /**
     * Password validation result for enterprise compliance.
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        
        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}