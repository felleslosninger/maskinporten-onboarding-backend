package no.digdir.simplifiedonboarding.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class Oauth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${frontend.uri}")
    private String frontendUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String path = request.getRequestURI();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(frontendUri);
        uriBuilder.queryParam("error", exception.getMessage());

        switch (path) {
            case "/login/oauth2/code/ansattporten-2480" -> {
                uriBuilder.queryParam("auth", "admin");
            }
            case "/login/oauth2/code/ansattporten-5613" -> {
                uriBuilder.queryParam("auth", "custom");
            }
            default -> {
                uriBuilder.queryParam("auth", "unknown");
            }
        }

        response.setHeader("Location", uriBuilder.toUriString());
        response.setStatus(HttpStatus.FOUND.value());
    }
}
