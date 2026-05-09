# SAML 2.0 IdP Quickstart

This project hosts a minimal SAML 2.0 Identity Provider at `/saml2/idp` so
external Service Providers (SPs) can do SP-initiated SSO against it.

## Endpoints

| Endpoint                  | Purpose                                 |
| ------------------------- | --------------------------------------- |
| `GET /saml2/idp/metadata` | Public IdP metadata (entityID, signing X.509, SSO bindings). Hand it to your SP at registration time. |
| `POST/GET /saml2/idp/sso` | Receives the SP's `SAMLRequest` (HTTP-POST or HTTP-Redirect/deflate), authenticates the user, and returns an HTTP-POST auto-submit form with the signed `SAMLResponse`. |

## 1. Register your Service Provider

Authenticate as a user with `saml:sp:manage` (or `ROLE_ADMIN`) and POST:

```bash
curl -X POST http://localhost:8080/api/saml/service-providers \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "entityId": "https://my-app.example.com/saml/metadata",
    "name": "My App",
    "acsUrl": "https://my-app.example.com/saml/acs",
    "nameIdFormat": "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
    "signingCertPem": "-----BEGIN CERTIFICATE-----\n…\n-----END CERTIFICATE-----"
  }'
```

The IdP only issues assertions for SPs registered here — anything else
returns `403 Unknown or disabled service provider`.

## 2. Hand your SP the IdP metadata

```bash
curl http://localhost:8080/saml2/idp/metadata > idp-metadata.xml
# Import this XML into your SP (Salesforce, AWS, Spring Security SAML SP,
# any SAML-capable application).
```

The metadata advertises:

- `entityID = http://localhost:8080/saml2/idp` (overridable via
  `APP_SAML_ENTITY_ID`)
- A signing X.509 certificate for assertion verification
- HTTP-POST and HTTP-Redirect SSO endpoints at `/saml2/idp/sso`
- `NameIDFormat = emailAddress`

## 3. Initiate SSO from your SP

The SP redirects the user to `/saml2/idp/sso?SAMLRequest=…&RelayState=…`
with a Base64-encoded (and possibly deflated) `<samlp:AuthnRequest>`.

If the user is already authenticated in the IdP, the IdP renders an
auto-submit HTML form that POSTs `SAMLResponse` (Base64) to your SP's
ACS URL. If not, the user is redirected to `/` to log in first; on
return the same flow continues.

## Limitations / production notes

- The signing key + cert are **regenerated in-memory at every restart**.
  Bind-mount a JKS keystore (or use a managed KMS) so the JWKS / SAML
  trust chain stays stable across restarts.
- `WantAuthnRequestsSigned="false"` in the metadata — incoming AuthnRequest
  signatures are not validated yet. Add it before exposing this IdP
  outside a trusted network.
- No SLO (Single Logout) endpoint yet.
- NameID is always the username; map to a custom attribute (`email`,
  `employeeId`) per SP in a follow-up if you need it.
