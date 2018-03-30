package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.csv.bean.BuildResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Created by root on 16.08.15..
 */
public interface Generator {

    BuildResult validate() throws IOException, URISyntaxException, IllegalAccessException, ParseException;

    void generate();

    boolean isWorking();

    int getItemsLeft();

    Object download() throws IOException, URISyntaxException, IllegalAccessException, ParseException;

}
