package com.iamportfolio.oauth2.controller;

import com.iamportfolio.oauth2.service.ScopeDescriptionRegistry;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Backs the custom OAuth2 consent screen.
 * <p>
 * Spring Authorization Server redirects the user to "/consent?client_id=…&scope=…&state=…"
 * before issuing tokens (when ClientSettings.requireAuthorizationConsent is true).
 * The page renders pretty scope descriptions and POSTs back to /oauth2/authorize
 * with consent_action=approve plus the selected scope checkboxes.
 */
@Controller
public class ConsentController {

    @Autowired
    private RegisteredClientRepository clients;

    @Autowired
    private ScopeDescriptionRegistry descriptions;

    @GetMapping("/consent")
    @ResponseBody
    public void consent(@RequestParam("client_id") String clientId,
                        @RequestParam("scope") String scopes,
                        @RequestParam("state") String state,
                        HttpServletResponse response) throws Exception {
        RegisteredClient client = clients.findByClientId(clientId);
        if (client == null) {
            response.sendError(404, "Unknown client_id");
            return;
        }
        Set<String> requested = new LinkedHashSet<>(Arrays.asList(scopes.split(" ")));
        // openid is non-negotiable for OIDC: it's pre-checked and not toggleable.

        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter w = response.getWriter()) {
            w.write("""
                <!doctype html>
                <html lang="en"><head>
                <meta charset="utf-8"><title>Authorize</title>
                <link rel="stylesheet" href="/css/style.css">
                <link rel="stylesheet" href="/css/consent.css">
                </head><body>
                <main class="consent-card">
                  <h1>Authorize <em>%s</em></h1>
                  <p class="muted">This application is requesting access to your account. Pick the scopes to grant.</p>
                  <form method="POST" action="/oauth2/authorize">
                    <input type="hidden" name="client_id" value="%s"/>
                    <input type="hidden" name="state" value="%s"/>
                    <ul class="scope-list">
                """.formatted(escape(client.getClientName()), escape(clientId), escape(state)));

            for (String s : requested) {
                boolean isOpenid = OidcScopes.OPENID.equals(s);
                w.write(("""
                    <li>
                      <label>
                        <input type="checkbox" name="scope" value="%s" %s %s/>
                        <strong>%s</strong>
                        <span class="scope-desc">%s</span>
                      </label>
                    </li>
                    """).formatted(escape(s),
                        "checked",
                        isOpenid ? "disabled" : "",
                        escape(s),
                        escape(descriptions.describe(s))));
                if (isOpenid) {
                    // Disabled inputs aren't submitted; carry openid as hidden.
                    w.write("<input type=\"hidden\" name=\"scope\" value=\"openid\"/>");
                }
            }

            w.write("""
                    </ul>
                    <div class="consent-actions">
                      <button type="submit" class="btn btn-primary">Allow</button>
                      <button type="submit" class="btn btn-outline" name="cancel" value="true">Cancel</button>
                    </div>
                  </form>
                </main>
                </body></html>
                """);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
