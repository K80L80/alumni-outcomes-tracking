package org.hci.batchjobs.radredcapbatch.batch;

//import com.example.controller.RedcapController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BatchJobScheduler {
    @Scheduled(fixedRate = 10000) // Execute every 10 seconds
    public void scheduleSurveySending() {
        System.out.println("Hello, World!");
    }
}
