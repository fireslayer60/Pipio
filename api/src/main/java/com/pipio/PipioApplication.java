package com.pipio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableEncryptableProperties
public class PipioApplication {
    public static void main(String[] args) {
        SpringApplication.run(PipioApplication.class, args);
    }
}
