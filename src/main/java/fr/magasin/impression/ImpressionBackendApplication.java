package fr.magasin.impression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImpressionBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImpressionBackendApplication.class, args);
    }
}


