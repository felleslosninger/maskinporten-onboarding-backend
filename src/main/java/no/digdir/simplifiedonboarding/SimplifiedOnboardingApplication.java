package no.digdir.simplifiedonboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// TODO: Remove this comment.

@SpringBootApplication
@EnableConfigurationProperties({MaskinportenConfig.class})
public class SimplifiedOnboardingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimplifiedOnboardingApplication.class, args);
	}

}
