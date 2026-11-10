package com.nectux.magicgold;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MagicGoldApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicGoldApplication.class, args);
    }
}
