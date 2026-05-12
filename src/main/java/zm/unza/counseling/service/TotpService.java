package zm.unza.counseling.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * TOTP (Time-based One-Time Password) service — RFC 6238, compatible with Google Authenticator.
 */
@Service
public class TotpService {

    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW = 1; // allow 1 step tolerance in each direction

    /**
     * Generate a new random base32-encoded TOTP secret.
     */
    public String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * Build a provisioning URI (otpauth://) for QR code scanners.
     */
    public String buildOtpAuthUri(String secret, String accountName, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                encode(issuer), encode(accountName), secret, encode(issuer), CODE_DIGITS, TIME_STEP_SECONDS);
    }

    /**
     * Verify a user-submitted 6-digit code against the stored secret.
     * Allows ±1 time-step tolerance for clock drift.
     */
    public boolean verify(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) return false;
        long timeStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        byte[] keyBytes = base32Decode(secret);
        for (int i = -WINDOW; i <= WINDOW; i++) {
            if (totp(keyBytes, timeStep + i).equals(code)) return true;
        }
        return false;
    }

    // ---- private helpers ----

    private String totp(byte[] key, long counter) {
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(counterBytes);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP computation failed", e);
        }
    }

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    private byte[] base32Decode(String s) {
        s = s.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outputLen = s.length() * 5 / 8;
        byte[] output = new byte[outputLen];
        int buffer = 0, bitsLeft = 0, idx = 0;
        for (char c : s.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output[idx++] = (byte) (buffer >> bitsLeft);
            }
        }
        return output;
    }

    private String encode(String s) {
        return s.replace(" ", "%20").replace(":", "%3A");
    }
}
