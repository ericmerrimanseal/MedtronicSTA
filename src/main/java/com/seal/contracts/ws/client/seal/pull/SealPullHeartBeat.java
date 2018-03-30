package com.seal.contracts.ws.client.seal.pull;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by jantonak on 13/07/17.
 */
@Slf4j
@Service
public class SealPullHeartBeat {

    @Value("${seal.pull.enabled}")
    private boolean enabled;

    @Autowired
    private SealPullProducer parent;

    @Scheduled(cron = "0 0/30 * * * *")
    public void run() {
        log.info("Heartbeat started");
        if (enabled) {
            log.debug("Heartbeat tick");
            if (!parent.isAnyConsumerRunning()) {
                log.debug("no consumers running --> going to wake up the producer");
                parent.getDB().clear();
                parent.notifyThreads();
            } else {
                log.debug("Some consumers are running --> ignore");
            }
        }
    }
}
