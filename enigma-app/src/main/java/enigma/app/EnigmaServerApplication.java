package enigma.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "enigma")
public class EnigmaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnigmaServerApplication.class, args);
    }
}
