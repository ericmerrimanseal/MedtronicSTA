package com.seal.contracts.generator.file;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by jantonak on 13/06/17.
 */
public abstract class FolderInitializer {

    protected static final String JAR = "jar";
    private final String folderName;


    public FolderInitializer(String folderName) {
        this.folderName = folderName;
    }

    public void init() throws IOException, URISyntaxException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final URL resource = loader.getResource(folderName);
        if (resource.getProtocol().equals(JAR)) {
            handleJar();
        } else {
            handleNonJar();
        }
    }

    protected abstract void handleJar() throws URISyntaxException, IOException;

    protected abstract void handleNonJar() throws IOException;
}
