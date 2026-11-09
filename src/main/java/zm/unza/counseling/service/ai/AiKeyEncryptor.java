package zm.unza.counseling.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption for provider API keys at rest.
 *
 * Key material comes from {@code app.ai.encryption-key} (env {@code AI_ENCRYPTION_KEY}).
 * If that is unset it falls back to deriving a key from the JWT secret so the feature works
 * out of the box — but then rotating JWT_SECRET makes stored keys unreadable, so production
 * should set AI_ENCRYPTION_KEY explicitly.
 */
@Slf4j
@Component
public class AiKeyEncryptor {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final String VERSION_PREFIX = "v1:";

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AiKeyEncryptor(@Value("${app.ai.encryption-key:}") String configuredKey,
                          @Value("${jwt.secret:}") String jwtSecret) {
        String material = configuredKey;
        if (material == null || material.isBlank()) {
            log.warn("app.ai.encryption-key (AI_ENCRYPTION_KEY) is not set; deriving the AI key-encryption "
                    + "key from jwt.secret. Set AI_ENCRYPTION_KEY in production so rotating the JWT secret "
                    + "does not invalidate stored provider API keys.");
            material = "ai-provider-keys:" + jwtSecret;
        }
        this.key = new SecretKeySpec(sha256(material), "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array();
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt API key", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null || stored.isEmpty()) return null;
        try {
            String b64 = stored.startsWith(VERSION_PREFIX) ? stored.substring(VERSION_PREFIX.length()) : stored;
            byte[] all = Base64.getDecoder().decode(b64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, all, 0, IV_LENGTH));
            byte[] pt = cipher.doFinal(all, IV_LENGTH, all.length - IV_LENGTH);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Stored API key could not be decrypted (was AI_ENCRYPTION_KEY or JWT_SECRET changed?). "
                    + "Re-enter the key for this provider.", e);
        }
    }

    public static String hint(String plaintext) {
        if (plaintext == null || plaintext.length() < 4) return null;
        return plaintext.substring(plaintext.length() - 4);
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
