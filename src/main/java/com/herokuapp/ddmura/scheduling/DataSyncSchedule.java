package com.herokuapp.ddmura.scheduling;

import com.herokuapp.ddmura.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSyncSchedule {

    @Autowired
    LikeService likeService;

    //@Scheduled(fixedRate = 3600000) // 1 hour
    @Scheduled(fixedRate = 60000)
    public void sync() {
        likeService.syncWriteSetToDatabase();
        likeService.syncCountSetToDatabase();
    }
}
