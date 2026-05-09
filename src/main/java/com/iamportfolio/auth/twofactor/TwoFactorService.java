package com.iamportfolio.auth.twofactor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class TwoFactorService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorService.class);

    @Value("${app.2fa.issuer}")
    private String issuer;

    @Value("${app.2fa.app-name}")
    private String appName;

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;
    private final TimeProvider timeProvider;

    public TwoFactorService() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();
        this.codeGenerator = new DefaultCodeGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeImageAsBase64(String secret, String username) {
        try {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer(issuer)
                    .digits(6)
                    .period(30)
                    .build();

            byte[] imageData = qrGenerator.generate(data);
            return Base64.getEncoder().encodeToString(imageData);
        } catch (Exception e) {
            logger.error("Erro ao gerar QR Code: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar QR Code", e);
        }
    }

    public String generateQrCodeUrl(String secret, String username) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
            issuer, username, secret, issuer
        );
    }

    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            logger.error("Erro ao verificar cÃ³digo 2FA: {}", e.getMessage(), e);
            return false;
        }
    }

    public String getCurrentCode(String secret) {
        try {
            long currentBucket = timeProvider.getTime() / 30;
            return codeGenerator.generate(secret, currentBucket);
        } catch (Exception e) {
            logger.error("Erro ao gerar cÃ³digo atual: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                code.append(random.nextInt(10));
            }
            backupCodes.add(code.toString());
        }
        
        return backupCodes;
    }

    public boolean verifyBackupCode(String providedCode, List<String> storedCodes) {
        return storedCodes != null && storedCodes.contains(providedCode);
    }

    public List<String> removeUsedBackupCode(String usedCode, List<String> storedCodes) {
        if (storedCodes != null) {
            List<String> updatedCodes = new ArrayList<>(storedCodes);
            updatedCodes.remove(usedCode);
            return updatedCodes;
        }
        return new ArrayList<>();
    }

    public String backupCodesToString(List<String> codes) {
        return String.join(",", codes);
    }

    public List<String> stringToBackupCodes(String codesString) {
        if (codesString == null || codesString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(codesString.split(","));
    }
} 