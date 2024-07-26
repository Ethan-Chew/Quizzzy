package sg.edu.np.mad.quizzzy.Classes;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;


public class TOTPUtil {
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_SIZE = 16;

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30 seconds time step
    private static final int TOTP_DIGITS = 6; // 6 digit TOTP

    public static boolean verifyTOTP(String secretKey, String totpCode) {
        long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP;

        //generate totp code using secret from firebase and compares with user input
        String generatedCode = generateTOTP(secretKey, timeIndex);
        return totpCode.equals(generatedCode);


    }

    private static String generateTOTP(String secretKey, long timeIndex) {
        try {

            //decode the secretKey given into base32
            Base32 base32 = new Base32();
            byte[] key = base32.decode(secretKey);

            //convert timeIndex to byte array
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeIndex & 0xFF);
                timeIndex >>= 8;
            }

            //convert secretKey to secretKeySpec using SHA1 algorithm
            SecretKeySpec signKey = new SecretKeySpec(key, HMAC_ALGO);
            Mac mac = Mac.getInstance(HMAC_ALGO);

            //generate hash using secretKeySpec and data
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            //get 31st bit of the hash
            int offset = hash[hash.length - 1] & 0xF;
            int truncatedHash = hash[offset] & 0x7F;
            for (int i = 1; i < 4; i++) {
                truncatedHash <<= 8;
                truncatedHash |= hash[offset + i] & 0xFF;
            }

            //truncate hash to 6 digits
            truncatedHash %= Math.pow(10, TOTP_DIGITS);
            return String.format("%0" + TOTP_DIGITS + "d", truncatedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating TOTP", e);
        }
    }

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);

        // Encode in Base32
        StringBuilder base32 = new StringBuilder();
        for (byte b : bytes) {

            base32.append(BASE32_CHARS.charAt((b & 0xFF) % BASE32_CHARS.length()));
        }
        return base32.toString();
    }

    public static String getTOTPURI(String secret, String issuer, String account) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, account, secret, issuer);
    }


}
