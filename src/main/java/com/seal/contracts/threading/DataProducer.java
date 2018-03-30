package com.seal.contracts.threading;

import java.util.List;

/**
 * Created by jantonak on 28/06/17.
 */
public interface DataProducer<T extends Threadable, R> {

    T produce() throws InterruptedException;

    void processed(R result, T input) throws Exception;

    void processedInternal(R result, T input);

    List<T> retrieve();

    void init();

    void error(Exception exception, T item);

    void notifyThreads();

    int getNumberOfConsumers();

    List<DataConsumer> getConsumers();

}
