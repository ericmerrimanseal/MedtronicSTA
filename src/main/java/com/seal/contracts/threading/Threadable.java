package com.seal.contracts.threading;

/**
 * Created by jantonak on 29/06/17.
 */
public interface Threadable {

    void lock();

    void unlock(boolean success);

}
