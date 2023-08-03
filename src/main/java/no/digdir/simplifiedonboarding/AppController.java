package no.digdir.simplifiedonboarding;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AppController {

    public static final String CLIENT_REGISTRATION_ID = "ansattporten-2480";
    @Value("${proxy.uri}")
    private String proxyUri;

    @Value("${frontend.uri}")
    private String frontendUri;

    @GetMapping("/user")
    public Map<String, Object> getAuthenticatedUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            return principal.getAttributes();
        }
        return null;
    }

    @GetMapping("/authenticate")
    public void authenticate(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", frontendUri + "/dashboard");
        httpServletResponse.setStatus(302);
    }

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @GetMapping("/datasharing/**")
    public ResponseEntity<?> proxyPath(ProxyExchange<byte[]> proxy, Authentication authentication,
                                       HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();


        return proxy.uri(proxyUri + proxy.path("/api"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken.getTokenValue())
                .get();
    }

    @PostMapping("/datasharing/**")
    public ResponseEntity<?> proxyPathPost(ProxyExchange<byte[]> proxy, Authentication authentication,
                                       HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) throws IOException {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();


        return proxy.uri(proxyUri + proxy.path("/api"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken.getTokenValue())
                .body(servletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())))
                .post();
    }

    @DeleteMapping("/datasharing/**")
    public ResponseEntity<?> proxyPathDelete(ProxyExchange<byte[]> proxy, Authentication authentication,
                                             HttpServletRequest servletRequest,
                                             HttpServletResponse servletResponse,
                                             @RequestParam String client_id) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        return proxy.uri(proxyUri + proxy.path("/api") + "?client_id=" + client_id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken.getTokenValue())
                .delete();
    }
}
