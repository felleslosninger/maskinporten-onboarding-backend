package no.digdir.simplifiedonboarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyEnvironmentController.class);

    @Autowired
    private MaskinportenConfig maskinportenConfig;

    @Value("${spring.application.environment}")
    private String environment;

    @GetMapping("/userinfo")
    public Map<String, Object > getAuthenticatedPrincipal(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            HashMap<String, Object> userinfo = new HashMap<>();
            String name = principal.getAttribute("name");
            userinfo.put("name", name);
            if (principal.getAttribute("authorization_details") != null) {
                List authorization_details = principal.getAttribute("authorization_details");
                List reportees = (List)((Map<String, Object>) authorization_details.get(0)).get("reportees");
                Object reporteeName = ((Map<String, Object>) reportees.get(0)).get("Name");
                Object reporteeId = ((Map<String, Object>) reportees.get(0)).get("ID");
                userinfo.put("reporteeName",reporteeName.toString());
                userinfo.put("reporteeId", reporteeId.toString().split(":")[1]);
                userinfo.put("trusted", true);
            } else {
                Map<String, Object> org = principal.getAttribute("user_org");
                userinfo.put("reporteeId", org.get("ID").toString().split(":")[1]);
                userinfo.put("trusted", false);
            }
            return userinfo;
        }
        return new HashMap<>();
    }

    @GetMapping("/config")
    public Map<String, Map<String, String>> getConfiguration() throws Throwable {
        logger.info("Fetching config");
        HashMap<String, Map<String, String>> map = new LinkedHashMap<>();

        //This should be rewritten to fetch issuer and token_endpoint from config.getAuthorizationServer() + "/.well-known/oauth-authorization-server". in accordance with https://datatracker.ietf.org/doc/html/rfc8414#section-3

        for ( String e : maskinportenConfig.getEnvironments()) {
            MaskinportenConfig.EnvironmentConfig config = maskinportenConfig.getConfigFor(e);
            HashMap<String, String> envSpesifics = new LinkedHashMap<>();
            envSpesifics.put("issuer", config.getAuthorizationServer()+ "/");
            envSpesifics.put("token_endpoint", config.getAuthorizationServer()+ "/token");
            envSpesifics.put("authorization_server", config.getAuthorizationServer());
            map.put(e, envSpesifics);
        }

        return map;
    }

    @GetMapping("/shouldSign")
    public boolean getSignatureStatus(@RequestParam("org") String orgnr) throws Throwable {
        logger.info("Fetching org sign status");
        String fileName;

        if (environment.equals("prod")) {
            fileName = "classpath:signedOrgs.csv";
        } else {
            fileName = "classpath:signedOrgsTest.csv";
        }

        List<String> signedOrgs = new ArrayList<>();
        Path path = Paths.get(ResourceUtils.getFile(fileName).toURI());
        Resource resource = new ByteArrayResource(Files.readAllBytes(path));
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            List<String> values = Arrays.stream(line.split(",")).toList();
            signedOrgs.addAll(values);
        }


        return !signedOrgs.contains(orgnr);
    }

}
