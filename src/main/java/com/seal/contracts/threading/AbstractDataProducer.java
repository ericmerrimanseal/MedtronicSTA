package com.seal.contracts.threading;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jantonak on 28/06/17.
 */
@Slf4j
public abstract class AbstractDataProducer<T extends Threadable, R, C extends DataConsumer<T, R>> implements DataProducer<T, R> {

    private LinkedList<T> items = Lists.newLinkedList();

    @Getter
    private List<DataConsumer> consumers = Lists.newArrayList();

    @Override
    public T produce() throws InterruptedException {
        synchronized (this) {
            if (items.isEmpty()) {
                this.items.addAll(retrieve());
                if (this.items.isEmpty()) {
                    log.debug("no more items -> wait");
                } else {
                    return items.poll();
                }
            }
            return Optional.fromNullable(items.poll()).orNull();
        }
    }

    @Override
    public void processedInternal(R result, T input) {
        try {
            processed(result, input);
        } catch (Exception e) {
            error(e, input);
        }
    }


    public boolean isAnyConsumerRunning() {
        return FluentIterable.from(getConsumers()).anyMatch(new com.google.common.base.Predicate<DataConsumer>() {
            @Override
            public boolean apply(DataConsumer consumer) {
                return consumer.isRunning();
            }
        });
    }

    @Override
    public int getNumberOfConsumers() {
        return consumers.size();
    }

    public void init() {
        Preconditions.checkArgument(poolSize() > 0, "PoolSize must be grater than 0");
        int numberOfConsumersToCreate = poolSize() - getConsumers().size();
        for (int i = 0; i < numberOfConsumersToCreate; i++) {
            C consumer = null;
            try {
                consumer = createNewConsumer();
                getConsumers().add(consumer);
                consumer.start();
            } catch (Exception e) {
                log.error("Unable to initialize the Consumer --> {}", e);
                e.printStackTrace();
            }
        }
    }

    public abstract C createNewConsumer() throws Exception;

    public abstract int poolSize();

    @Override
    public void notifyThreads() {
        init();
    }
}
