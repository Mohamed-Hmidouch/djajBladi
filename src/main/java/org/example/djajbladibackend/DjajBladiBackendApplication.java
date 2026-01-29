package org.example.djajbladibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DjajBladiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DjajBladiBackendApplication.class, args);
    }

}