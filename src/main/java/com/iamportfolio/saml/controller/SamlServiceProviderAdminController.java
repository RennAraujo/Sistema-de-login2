package com.iamportfolio.saml.controller;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.saml.dto.SamlSpCreateRequest;
import com.iamportfolio.saml.dto.SamlSpResponse;
import com.iamportfolio.saml.model.SamlServiceProvider;
import com.iamportfolio.saml.repository.SamlServiceProviderRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

/**
 * SAML SP registry CRUD. Gated by saml:sp:manage / ROLE_ADMIN.
 * The IdP only issues assertions to SPs in this table — registering an
 * SP here is what makes /saml2/idp/sso accept its AuthnRequest.
 */
@RestController
@RequestMapping("/api/saml/service-providers")
public class SamlServiceProviderAdminController {

    @Autowired
    private SamlServiceProviderRepository repository;

    @GetMapping
    public List<SamlSpResponse> list() {
        return repository.findAll().stream().map(SamlSpResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SamlSpResponse get(@PathVariable Long id) {
        return SamlSpResponse.from(loadOrThrow(id));
    }

    @PostMapping
    @Auditable(value = "SAML_SP_CREATE", resourceType = "SAML_SP")
    public SamlSpResponse create(@Valid @RequestBody SamlSpCreateRequest req) {
        if (repository.findByEntityId(req.entityId()).isPresent()) {
            throw new IllegalStateException("SP already registered: " + req.entityId());
        }
        validatePem(req.signingCertPem());

        SamlServiceProvider sp = new SamlServiceProvider();
        sp.setEntityId(req.entityId());
        sp.setName(req.name());
        sp.setAcsUrl(req.acsUrl());
        sp.setNameIdFormat(req.nameIdFormat());
        sp.setSigningCertPem(req.signingCertPem());
        sp.setEnabled(true);
        return SamlSpResponse.from(repository.save(sp));
    }

    @DeleteMapping("/{id}")
    @Auditable(value = "SAML_SP_DELETE", resourceType = "SAML_SP")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.delete(loadOrThrow(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @Auditable(value = "SAML_SP_DISABLE", resourceType = "SAML_SP")
    public SamlSpResponse disable(@PathVariable Long id) {
        SamlServiceProvider sp = loadOrThrow(id);
        sp.setEnabled(false);
        return SamlSpResponse.from(repository.save(sp));
    }

    @PostMapping("/{id}/enable")
    @Auditable(value = "SAML_SP_ENABLE", resourceType = "SAML_SP")
    public SamlSpResponse enable(@PathVariable Long id) {
        SamlServiceProvider sp = loadOrThrow(id);
        sp.setEnabled(true);
        return SamlSpResponse.from(repository.save(sp));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }

    private SamlServiceProvider loadOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SP not found: " + id));
    }

    private static void validatePem(String pem) {
        if (pem == null || pem.isBlank()) return;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cf.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
        } catch (CertificateException e) {
            throw new IllegalStateException("Invalid signing_cert_pem: " + e.getMessage());
        }
    }
}
