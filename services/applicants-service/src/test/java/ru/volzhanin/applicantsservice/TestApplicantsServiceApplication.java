package ru.volzhanin.applicantsservice;

import org.springframework.boot.SpringApplication;

public class TestApplicantsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(ApplicantsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
