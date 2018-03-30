package com.seal.contracts.generator.csv.filter;

/**
 * Created by root on 26.08.15..
 */
public interface Filter<T> {

    boolean exclude(T object);

}
