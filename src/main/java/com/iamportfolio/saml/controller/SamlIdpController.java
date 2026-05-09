package com.iamportfolio.saml.controller;

import com.iamportfolio.common.audit.Auditable;
import com.iamportfolio.saml.model.SamlServiceProvider;
import com.iamportfolio.saml.repository.SamlServiceProviderRepository;
import com.iamportfolio.saml.service.SamlAssertionBuilder;
import com.iamportfolio.saml.service.SamlIdpKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Minimal SAML 2.0 Identity Provider.
 * <p>
 * Implements the two endpoints an SP needs:
 *   <ul>
 *     <li><code>GET /saml2/idp/metadata</code> — IdP metadata XML (entityID, signing cert, SSO endpoint).</li>
 *     <li><code>POST/GET /saml2/idp/sso</code> — receives the SP's AuthnRequest, validates the requesting
 *         entity is registered, builds + signs a SAML Response, and renders an auto-submit HTML form
 *         that POSTs the Base64 SAMLResponse to the SP's ACS URL (HTTP-POST binding).</li>
 *   </ul>
 * <p>
 * Anonymous users hitting /sso are redirected to the form login at "/" first; on return they're
 * authenticated and the assertion is built with their username as NameID.
 */
@RestController
@RequestMapping("/saml2/idp")
public class SamlIdpController {

    private static final Logger logger = LoggerFactory.getLogger(SamlIdpController.class);

    @Autowired
    private SamlIdpKeyManager keyManager;

    @Autowired
    private SamlServiceProviderRepository spRepository;

    @Autowired
    private SamlAssertionBuilder assertionBuilder;

    @Value("${app.saml.entity-id:http://localhost:8080/saml2/idp}")
    private String entityId;

    @Value("${app.saml.sso-url:http://localhost:8080/saml2/idp/sso}")
    private String ssoUrl;

    @GetMapping(value = "/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> metadata() {
        String cert = keyManager.getCertificateBase64();
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata"
                                     xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                                     entityID="%s">
                  <md:IDPSSODescriptor WantAuthnRequestsSigned="false"
                                       protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                    <md:KeyDescriptor use="signing">
                      <ds:KeyInfo>
                        <ds:X509Data>
                          <ds:X509Certificate>%s</ds:X509Certificate>
                        </ds:X509Data>
                      </ds:KeyInfo>
                    </md:KeyDescriptor>
                    <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>
                    <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                                            Location="%s"/>
                    <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
                                            Location="%s"/>
                  </md:IDPSSODescriptor>
                </md:EntityDescriptor>
                """.formatted(entityId, cert, ssoUrl, ssoUrl);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
    }

    @RequestMapping(value = "/sso", method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.TEXT_HTML_VALUE)
    @Auditable(value = "SAML_SSO", resourceType = "SAML_SP")
    public ResponseEntity<String> sso(@RequestParam("SAMLRequest") String samlRequest,
                                      @RequestParam(value = "RelayState", required = false) String relayState) throws Exception {
        AuthnRequest decoded = decodeAuthnRequest(samlRequest);
        Optional<SamlServiceProvider> sp = spRepository.findByEntityId(decoded.issuer);
        if (sp.isEmpty() || !sp.get().isEnabled()) {
            logger.warn("SAML SSO rejected: unknown or disabled SP {}", decoded.issuer);
            return ResponseEntity.status(403).body("Unknown or disabled service provider: " + decoded.issuer);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(
                    "Not authenticated. Open the SP-initiated SSO flow after logging in at /."
            );
        }

        String response = assertionBuilder.build(sp.get(), auth.getName(), decoded.id);
        String html = """
                <!doctype html>
                <html><body onload="document.forms[0].submit()">
                <form method="POST" action="%s">
                  <input type="hidden" name="SAMLResponse" value="%s"/>
                  %s
                  <noscript><button type="submit">Continue to %s</button></noscript>
                </form>
                </body></html>
                """.formatted(
                escape(sp.get().getAcsUrl()),
                response,
                relayState == null ? "" : "<input type=\"hidden\" name=\"RelayState\" value=\"" + escape(relayState) + "\"/>",
                escape(sp.get().getName())
        );
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    private AuthnRequest decodeAuthnRequest(String samlRequest) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(samlRequest);
        // HTTP-Redirect binding deflates; HTTP-POST does not. Try both.
        byte[] xml;
        try {
            xml = new InflaterInputStream(new ByteArrayInputStream(bytes), new Inflater(true)).readAllBytes();
        } catch (Exception e) {
            xml = bytes;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        Element root = doc.getDocumentElement();
        AuthnRequest req = new AuthnRequest();
        req.id = root.getAttribute("ID");
        var issuerNodes = root.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer");
        if (issuerNodes.getLength() > 0) req.issuer = issuerNodes.item(0).getTextContent();
        return req;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }

    static class AuthnRequest {
        String id;
        String issuer;
    }
}
