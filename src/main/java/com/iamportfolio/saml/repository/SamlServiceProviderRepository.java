package com.iamportfolio.saml.repository;

import com.iamportfolio.saml.model.SamlServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SamlServiceProviderRepository extends JpaRepository<SamlServiceProvider, Long> {
    Optional<SamlServiceProvider> findByEntityId(String entityId);
}
