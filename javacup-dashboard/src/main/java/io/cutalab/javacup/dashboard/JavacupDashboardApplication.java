package io.cutalab.javacup.dashboard;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JavacupDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavacupDashboardApplication.class, args);
    }
}
