package no.digdir.simplifiedonboarding;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ConfigurationProperties()
public class MaskinportenConfig {

    private List<EnvironmentConfig> maskinportenConfig;

    public List<EnvironmentConfig> getMaskinportenConfig() {
        return maskinportenConfig;
    }

    public void setMaskinportenConfig(List<EnvironmentConfig> maskinportenConfig) {
        this.maskinportenConfig = maskinportenConfig;
    }

    public List<String> getEnvironments(){
        return getMaskinportenConfig()
                .stream()
                .map(EnvironmentConfig::getEnvironment)
                .collect(Collectors.toList());
    }

    public EnvironmentConfig getConfigFor(String environment) throws Throwable {
        Optional<EnvironmentConfig> environmentConfigOptional = getMaskinportenConfig()
                .stream()
                .filter(environmentConfig -> environmentConfig.getEnvironment().equalsIgnoreCase(environment))
                .findFirst();
        return environmentConfigOptional.orElseThrow((Supplier<Throwable>) UnsupportedOperationException::new);
    }

    public static class EnvironmentConfig {

        private String environment;
        private String authorizationServer;
        private String api;

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getAuthorizationServer() {
            return authorizationServer;
        }

        public void setAuthorizationServer(String authorizationServer) {
            this.authorizationServer = authorizationServer;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }
    }
}
