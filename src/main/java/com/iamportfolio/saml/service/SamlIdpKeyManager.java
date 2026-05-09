package com.iamportfolio.saml.service;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

/**
 * Provides the RSA 2048 signing key + a self-signed X.509 certificate the
 * IdP advertises in its metadata and uses to sign SAML assertions.
 * <p>
 * Generated in-memory at boot using BouncyCastle — fine for portfolio /
 * dev. Production should pin a long-lived keystore so SP trust chains
 * stay valid across restarts.
 */
@Component
public class SamlIdpKeyManager {

    private static final Logger logger = LoggerFactory.getLogger(SamlIdpKeyManager.class);

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X509Certificate certificate;
    private String certificatePem;

    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        this.certificate = buildSelfSigned(pair);
        this.certificatePem = toPem(this.certificate);
        logger.warn("Generated ephemeral SAML IdP signing key + self-signed cert. " +
                "Bind-mount a real keystore in production so SP trust chains stay valid.");
    }

    private X509Certificate buildSelfSigned(KeyPair pair) throws Exception {
        X500Name name = new X500Name("CN=iam-portfolio-saml-idp");
        BigInteger serial = new BigInteger(64, new SecureRandom());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                name, serial, notBefore, notAfter, name, pair.getPublic()
        );
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(pair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }

    public PrivateKey getPrivateKey() { return privateKey; }
    public PublicKey getPublicKey() { return publicKey; }
    public X509Certificate getCertificate() { return certificate; }

    /** Base64-encoded DER (one-line) suitable for &lt;ds:X509Certificate&gt;. */
    public String getCertificateBase64() {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String getCertificatePem() { return certificatePem; }

    private static String toPem(X509Certificate cert) throws Exception {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(cert.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + b64 + "\n-----END CERTIFICATE-----\n";
    }
}
