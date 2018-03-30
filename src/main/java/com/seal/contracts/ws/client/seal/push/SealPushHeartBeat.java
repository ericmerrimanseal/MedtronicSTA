package com.seal.contracts.ws.client.seal.push;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by jantonak on 13/07/17.
 */
@Slf4j
@Service
public class SealPushHeartBeat extends Thread {

    @Value("${seal.sync.enabled}")
    private boolean enabled;

    @Autowired
    private SealSyncProducer parent;

    @PostConstruct
    private void init() {
        if (enabled) {
            start();
        }
    }

    @Override
    public void run() {
        log.info("Heartbeat started");
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (true) {
            if (stopwatch.elapsed(TimeUnit.SECONDS) >= 10) {
                log.debug("Heartbeat tick");
                if (!parent.isAnyConsumerRunning()) {
                    log.debug("no consumers running --> going to wake up the producer");
                    parent.notifyThreads();
                } else {
                    log.debug("Some consumers are running --> ignore");
                }
                stopwatch.reset().start();
            }
            try {
                sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
