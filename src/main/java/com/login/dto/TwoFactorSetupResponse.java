package com.login.dto;

import java.util.List;

public class TwoFactorSetupResponse {

    private String secretKey;
    private String qrCodeImage;
    private String qrCodeUrl;
    private List<String> backupCodes;
    private String message;
    private boolean success;

    // Construtores
    public TwoFactorSetupResponse() {}

    public TwoFactorSetupResponse(String secretKey, String qrCodeImage, String qrCodeUrl, List<String> backupCodes) {
        this.secretKey = secretKey;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeUrl = qrCodeUrl;
        this.backupCodes = backupCodes;
        this.success = true;
        this.message = "Configuração de 2FA realizada com sucesso";
    }

    // Factory methods
    public static TwoFactorSetupResponse success(String secretKey, String qrCodeImage, String qrCodeUrl, List<String> backupCodes) {
        return new TwoFactorSetupResponse(secretKey, qrCodeImage, qrCodeUrl, backupCodes);
    }

    public static TwoFactorSetupResponse error(String message) {
        TwoFactorSetupResponse response = new TwoFactorSetupResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Getters e Setters
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public List<String> getBackupCodes() {
        return backupCodes;
    }

    public void setBackupCodes(List<String> backupCodes) {
        this.backupCodes = backupCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "TwoFactorSetupResponse{" +
                "qrCodeUrl='" + qrCodeUrl + '\'' +
                ", backupCodesCount=" + (backupCodes != null ? backupCodes.size() : 0) +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
} 