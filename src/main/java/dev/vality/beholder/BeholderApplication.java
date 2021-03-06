package dev.vality.beholder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class BeholderApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeholderApplication.class, args);
    }

}
