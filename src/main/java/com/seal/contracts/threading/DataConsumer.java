package com.seal.contracts.threading;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.seal.contracts.threading.Status.IDLE;
import static com.seal.contracts.threading.Status.RUNNING;

/**
 * Created by jantonak on 28/06/17.
 */
@Slf4j
public abstract class DataConsumer<T extends Threadable, R extends Object> extends Thread {

    @Getter
    private Status status;

    private boolean terminate;

    @Getter
    protected final DataProducer<T, R> producer;

    protected DataConsumer(DataProducer<T, R> producer) {
        this.producer = producer;
        setName(String.format("%s_%s", this.getClass().getSimpleName(), producer.getNumberOfConsumers() + 1));
    }

    @Override
    public void run() {
        log.debug("started!");
        while (!terminate) {
            T item = null;
            try {
                status = IDLE;
                item = producer.produce();
                if (item != null) {
                    status = RUNNING;
                    R result = process(item);
                    producer.processedInternal(result, item);
                } else {
                    terminate = true;
                }
            } catch (Exception e) {
                producer.error(e, item);
            }
        }
        producer.getConsumers().remove(this);
        log.debug("Terminated");

    }

    public abstract R process(T item) throws Exception;

    public boolean isRunning() {
        boolean isCurrentThread = Thread.currentThread().equals(this);
        if (!isCurrentThread) {
            switch (getState()) {
                case RUNNABLE:
                case BLOCKED:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
}
