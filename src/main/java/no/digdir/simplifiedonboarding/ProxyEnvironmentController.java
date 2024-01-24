package no.digdir.simplifiedonboarding;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{env}")
public class ProxyEnvironmentController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyEnvironmentController.class);

    @Autowired
    private MaskinportenConfig maskinportenConfig;
    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;


    @GetMapping("/datasharing/**")
    public ResponseEntity<?> proxyPathToEnv(ProxyExchange<byte[]> proxy, Authentication authentication,
                                            HttpServletRequest servletRequest,
                                            HttpServletResponse servletResponse,
                                            @PathVariable("env") String environment) throws Throwable {
        OAuth2AccessToken accessToken = getAccessToken(authentication, servletRequest, servletResponse);

        MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(environment);
        String queries =servletRequest.getQueryString();
        String uri = config.getApi() + proxy.path("/api/" + config.getEnvironment());
        if (queries != null) {
            uri += "?" + queries;
        }
        logger.info("GET to {}", uri);
        var response = proxy.uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .get();
        var headers = response.getHeaders();
        logger.info("proxy response headers: {}", response.getHeaders());

        headers.remove("Set-Cookie");
        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    @GetMapping("/**")
    public ResponseEntity<?> proxyPathToEnvPublic(ProxyExchange<byte[]> proxy,
                                            HttpServletRequest servletRequest,
                                            @PathVariable("env") String environment) throws Throwable {
        MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(environment);
        String queries =servletRequest.getQueryString();
        String uri = config.getApi() + proxy.path("/api/" + config.getEnvironment());
        if (queries != null) {
            uri += "?" + queries;
        }
        logger.info("GET to {}", uri);
        return proxy.uri(uri)
                .get(response -> new ResponseEntity<>(response.getBody(), response.getStatusCode()));
    }


    @PostMapping("/datasharing/**")
    public ResponseEntity<?> proxyPathToEnvPost(ProxyExchange<byte[]> proxy, Authentication authentication,
                                                HttpServletRequest servletRequest,
                                                HttpServletResponse servletResponse,
                                                @PathVariable("env") String environment) throws Throwable {
        OAuth2AccessToken accessToken = getAccessToken(authentication, servletRequest, servletResponse);

        MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(environment);
        String uri = config.getApi() + proxy.path("/api/" + config.getEnvironment());
        logger.info("POST to {}", uri);
        return proxy.uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue())
                .body(servletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())))
                .post(response -> new ResponseEntity<>(response.getBody(), response.getStatusCode()));
    }

    @DeleteMapping("/datasharing/**")
    public ResponseEntity<?> proxyPathDelete(ProxyExchange<byte[]> proxy, Authentication authentication,
                                             HttpServletRequest servletRequest,
                                             HttpServletResponse servletResponse,
                                             @PathVariable("env") String environment,
                                             @RequestParam String client_id) throws Throwable {
        OAuth2AccessToken accessToken = getAccessToken(authentication, servletRequest, servletResponse);

        MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(environment);
        String uri = config.getApi() + proxy.path("/api/" + config.getEnvironment()) + "?client_id=" + client_id;
        logger.info("DELETE to {}", uri);
        return proxy.uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+accessToken.getTokenValue())
                .delete(response -> new ResponseEntity<>(response.getBody(), response.getStatusCode()));
    }


    private OAuth2AccessToken getAccessToken(Authentication authentication, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String clientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                .principal(authentication)
                .attributes(attrs -> {
                    attrs.put(HttpServletRequest.class.getName(), servletRequest);
                    attrs.put(HttpServletResponse.class.getName(), servletResponse);
                })
                .build();
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);
        return authorizedClient.getAccessToken();
    }
}
