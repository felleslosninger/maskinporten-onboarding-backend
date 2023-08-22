package no.digdir.simplifiedonboarding;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    @Value("${frontend.uri}")
    private String frontendUri;

    @Autowired
    private MaskinportenConfig maskinportenConfig;

    @GetMapping("/userinfo")
    public Map<String, String > getAuthenticatedPrincipal(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            HashMap<String, String> userinfo = new HashMap<>();
            String name = principal.getAttribute("name");
            userinfo.put("name", name);
            List authorization_details = principal.getAttribute("authorization_details");
            List reportees = (List)((Map<String, Object>) authorization_details.get(0)).get("reportees");
            Object reporteeName = ((Map<String, Object>) reportees.get(0)).get("Name");
            userinfo.put("reporteeName",reporteeName.toString());
            return userinfo;
        }
        return new HashMap<>();
    }

    @GetMapping("/authenticate")
    public void authenticate(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", frontendUri + "/dashboard");
        httpServletResponse.setStatus(302);
    }


    @GetMapping("/config")
    public Map<String, Map<String, String>> getConfiguration() throws Throwable {
        HashMap<String, Map<String, String>> map = new HashMap<>();

        //This should be rewritten to fetch issuer and token_endpoint from config.getAuthorizationServer() + "/.well-known/oauth-authorization-server". in accordance with https://datatracker.ietf.org/doc/html/rfc8414#section-3

        for ( String e : maskinportenConfig.getEnvironments()) {
            MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(e);
            HashMap<String, String> envSpesifics = new HashMap<>();
            envSpesifics.put("issuer", config.getAuthorizationServer()+ "/");
            envSpesifics.put("token_endpoint", config.getAuthorizationServer()+ "/token");
            envSpesifics.put("authorization_server", config.getAuthorizationServer());
            map.put(e, envSpesifics);
        }
        return map;
    }

}
