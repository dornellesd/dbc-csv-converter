package com.example.converter.Scheduled;

import com.example.converter.Utils.FileConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTask {

    @Scheduled(cron = "0/5 * * * * ?") // Run every 5 seconds
    public void executeTask() {
        System.out.println("Scheduled task executed at: " + System.currentTimeMillis());
        FileConverter.convertFiles();
    }
}
