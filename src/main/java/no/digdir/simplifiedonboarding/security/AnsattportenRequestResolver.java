package no.digdir.simplifiedonboarding.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;


public class AnsattportenRequestResolver implements OAuth2AuthorizationRequestResolver {
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        String role = request.getParameter("role");
        if(role in ("hovedadmin, enkelttjeneste")){
            map to number
        }
        OAuth2AuthorizationRequest.authorizationCode()
                .additionalParameters(request.getParameter("id")., )
        return ;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return null;
    }
}
