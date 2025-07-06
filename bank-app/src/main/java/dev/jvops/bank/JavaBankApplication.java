package dev.jvops.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class JavaBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaBankApplication.class, args);
    }
}