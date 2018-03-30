package com.seal.contracts.ws.client.seal.push;

import com.google.common.base.Joiner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Juraj on 19.07.2017.
 */
public class SealURL {
    private final String baseUrl;

    public SealURL(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URI getNonceURL() throws URISyntaxException {
        return new URI(Joiner.on("/").join(baseUrl, "security", "nonce"));
    }

    public URI login() throws URISyntaxException {
        return new URI(Joiner.on("/").join(baseUrl, "auths"));
    }

    public URI updateMetadata(String hash, String fieldName) throws URISyntaxException {
        return new URI(Joiner.on("/").join(baseUrl, "contracts", hash, "metadata", fieldName));
    }

    public URI updateMetadataBulk(String hash) throws URISyntaxException {
        return new URI(Joiner.on("/").join(baseUrl, "contracts", hash, "metadata"));
    }

    public URI getMetadata(String hash) throws URISyntaxException {
        String woParams = Joiner.on("/").join(baseUrl, "contracts", hash);
        return new URI(Joiner.on("?").join(woParams, "expand=metadata"));
    }

    public URI getStoredFile(String hash) throws URISyntaxException {
        return new URI(Joiner.on("/").join(baseUrl, "downloads", hash));
    }

}


