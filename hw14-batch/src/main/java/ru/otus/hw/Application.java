package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EntityScan(basePackages = { "ru.otus.hw.h2Models", "ru.otus.hw.mongoModels" })
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
