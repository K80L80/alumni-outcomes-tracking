package org.hci.batchjobs.radredcapbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduling support
public class SpringTesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTesterApplication.class, args);
    }

}
