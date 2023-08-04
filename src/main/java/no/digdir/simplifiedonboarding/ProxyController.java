package no.digdir.simplifiedonboarding;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Deprecated
@RestController
@RequestMapping("/api/datasharing")
public class ProxyController {

    public static final String CLIENT_REGISTRATION_ID = "ansattporten-2480";

    @Autowired
    private MaskinportenConfig maskinportenConfig;
    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @GetMapping("/**")
    public ResponseEntity<?> proxyPath(ProxyExchange<byte[]> proxy, Authentication authentication,
                                       HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) throws Throwable {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();


        return proxy.uri(maskinportenConfig.getConfigFor("test").getApi() + proxy.path("/api"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .get();
    }


    @PostMapping("/**")
    public ResponseEntity<?> proxyPathPost(ProxyExchange<byte[]> proxy, Authentication authentication,
                                           HttpServletRequest servletRequest,
                                           HttpServletResponse servletResponse) throws Throwable {
        MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor("test");
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();


        return proxy.uri((config.getApi() + proxy.path("/api/" + config.getEnvironment())) + proxy.path("/api"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .body(servletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())))
                .post();
    }

}
