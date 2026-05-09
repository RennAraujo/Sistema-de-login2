package com.iamportfolio.saml.service;

import com.iamportfolio.saml.model.SamlServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Builds a SAML 2.0 Response with a signed Assertion using the JDK's
 * built-in XML Signature API (no extra deps). The output is the
 * Base64-encoded XML expected by the HTTP-POST binding's SAMLResponse
 * form parameter.
 */
@Component
public class SamlAssertionBuilder {

    private static final String SAML_PROTOCOL_NS = "urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML_ASSERTION_NS = "urn:oasis:names:tc:SAML:2.0:assertion";

    @Autowired
    private SamlIdpKeyManager keyManager;

    @Value("${app.saml.entity-id:http://localhost:8080/saml2/idp}")
    private String idpEntityId;

    /**
     * Build, sign, and Base64-encode the SAML Response for the given subject.
     *
     * @param sp        registered service provider (audience + ACS URL)
     * @param subject   authenticated username (becomes the NameID)
     * @param requestId in-response-to value from the SP's AuthnRequest
     * @return Base64(UTF-8(signed XML)) ready for the SAMLResponse form field
     */
    public String build(SamlServiceProvider sp, String subject, String requestId) throws Exception {
        Instant now = Instant.now();
        Instant exp = now.plus(5, ChronoUnit.MINUTES);
        String responseId = "_R" + UUID.randomUUID().toString().replace("-", "");
        String assertionId = "_A" + UUID.randomUUID().toString().replace("-", "");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element response = doc.createElementNS(SAML_PROTOCOL_NS, "samlp:Response");
        response.setAttribute("ID", responseId);
        response.setAttribute("Version", "2.0");
        response.setAttribute("IssueInstant", now.toString());
        response.setAttribute("Destination", sp.getAcsUrl());
        if (requestId != null) response.setAttribute("InResponseTo", requestId);
        doc.appendChild(response);

        appendIssuer(doc, response, idpEntityId);

        Element status = doc.createElementNS(SAML_PROTOCOL_NS, "samlp:Status");
        Element statusCode = doc.createElementNS(SAML_PROTOCOL_NS, "samlp:StatusCode");
        statusCode.setAttribute("Value", "urn:oasis:names:tc:SAML:2.0:status:Success");
        status.appendChild(statusCode);
        response.appendChild(status);

        Element assertion = doc.createElementNS(SAML_ASSERTION_NS, "saml:Assertion");
        assertion.setAttribute("ID", assertionId);
        assertion.setAttribute("Version", "2.0");
        assertion.setAttribute("IssueInstant", now.toString());
        appendIssuer(doc, assertion, idpEntityId);

        Element subjectEl = doc.createElementNS(SAML_ASSERTION_NS, "saml:Subject");
        Element nameId = doc.createElementNS(SAML_ASSERTION_NS, "saml:NameID");
        nameId.setAttribute("Format",
                sp.getNameIdFormat() != null ? sp.getNameIdFormat()
                        : "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        nameId.setTextContent(subject);
        subjectEl.appendChild(nameId);

        Element subjectConfirmation = doc.createElementNS(SAML_ASSERTION_NS, "saml:SubjectConfirmation");
        subjectConfirmation.setAttribute("Method", "urn:oasis:names:tc:SAML:2.0:cm:bearer");
        Element subjectConfirmationData = doc.createElementNS(SAML_ASSERTION_NS, "saml:SubjectConfirmationData");
        subjectConfirmationData.setAttribute("NotOnOrAfter", exp.toString());
        subjectConfirmationData.setAttribute("Recipient", sp.getAcsUrl());
        if (requestId != null) subjectConfirmationData.setAttribute("InResponseTo", requestId);
        subjectConfirmation.appendChild(subjectConfirmationData);
        subjectEl.appendChild(subjectConfirmation);
        assertion.appendChild(subjectEl);

        Element conditions = doc.createElementNS(SAML_ASSERTION_NS, "saml:Conditions");
        conditions.setAttribute("NotBefore", now.toString());
        conditions.setAttribute("NotOnOrAfter", exp.toString());
        Element audienceRestriction = doc.createElementNS(SAML_ASSERTION_NS, "saml:AudienceRestriction");
        Element audience = doc.createElementNS(SAML_ASSERTION_NS, "saml:Audience");
        audience.setTextContent(sp.getEntityId());
        audienceRestriction.appendChild(audience);
        conditions.appendChild(audienceRestriction);
        assertion.appendChild(conditions);

        Element authnStatement = doc.createElementNS(SAML_ASSERTION_NS, "saml:AuthnStatement");
        authnStatement.setAttribute("AuthnInstant", now.toString());
        authnStatement.setAttribute("SessionIndex", "_S" + UUID.randomUUID());
        Element authnContext = doc.createElementNS(SAML_ASSERTION_NS, "saml:AuthnContext");
        Element authnContextClassRef = doc.createElementNS(SAML_ASSERTION_NS, "saml:AuthnContextClassRef");
        authnContextClassRef.setTextContent("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        authnContext.appendChild(authnContextClassRef);
        authnStatement.appendChild(authnContext);
        assertion.appendChild(authnStatement);

        response.appendChild(assertion);

        // Sign the assertion (enveloped XML signature on the Assertion element).
        signElement(doc, assertion, assertionId);

        return Base64.getEncoder().encodeToString(serialize(doc).getBytes("UTF-8"));
    }

    private void appendIssuer(Document doc, Element parent, String issuerValue) {
        Element issuer = doc.createElementNS(SAML_ASSERTION_NS, "saml:Issuer");
        issuer.setTextContent(issuerValue);
        parent.appendChild(issuer);
    }

    private void signElement(Document doc, Element element, String elementId) throws Exception {
        element.setIdAttribute("ID", true);

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<Transform> transforms = (List) Collections.unmodifiableList(java.util.Arrays.asList(
                fac.newTransform("http://www.w3.org/2000/09/xmldsig#enveloped-signature", (TransformParameterSpec) null),
                fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null)
        ));

        Reference ref = fac.newReference(
                "#" + elementId,
                fac.newDigestMethod(DigestMethod.SHA256, null),
                transforms,
                null,
                null
        );

        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#",
                        (C14NMethodParameterSpec) null),
                fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
                Collections.singletonList(ref)
        );

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509 = kif.newX509Data(Collections.singletonList(keyManager.getCertificate()));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509));

        XMLSignature signature = fac.newXMLSignature(si, ki);
        // Insert the Signature right after Issuer per SAML schema.
        DOMSignContext dsc = new DOMSignContext(keyManager.getPrivateKey(), element);
        NodeList issuerNodes = element.getElementsByTagNameNS(SAML_ASSERTION_NS, "Issuer");
        if (issuerNodes.getLength() > 0 && issuerNodes.item(0).getNextSibling() != null) {
            dsc.setNextSibling(issuerNodes.item(0).getNextSibling());
        }
        signature.sign(dsc);
    }

    private String serialize(Document doc) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
