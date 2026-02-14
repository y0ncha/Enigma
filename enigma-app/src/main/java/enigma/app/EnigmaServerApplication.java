package enigma.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "enigma")
@EnableJpaRepositories(basePackages = "enigma.dal.repository")
@EntityScan(basePackages = "enigma.dal.entity")
public class EnigmaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnigmaServerApplication.class, args);
    }
}
