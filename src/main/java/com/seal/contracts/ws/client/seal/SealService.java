package com.seal.contracts.ws.client.seal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.ws.client.seal.meta.SealContractsResponse;
import com.seal.contracts.ws.client.seal.meta.Value;
import com.seal.contracts.ws.client.seal.push.SealURL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Juraj on 01.08.2017.
 */
@Service
@Slf4j
public class SealService {

    private static final Duration TOKEN_CACHE = Duration.ofMinutes(5);

    private Instant lastLoginTime;

    MultiValueMap<String, String> login = new LinkedMultiValueMap();

    @Autowired
    private ConfigService configService;

    private final RestTemplate restTemplate = new RestTemplate();

    private SealURL sealURL;

    @PostConstruct
    private void init() {
        sealURL = new SealURL(configService.getSealUrl());
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
    }

    private MultiValueMap login() throws URISyntaxException {
        boolean isTokenExpired = lastLoginTime == null || Duration.between(lastLoginTime, Instant.now()).getSeconds() > TOKEN_CACHE.getSeconds();
        log.debug("Token is expired ?: {}", isTokenExpired);
        if (isTokenExpired) {
            String token = null;
            RequestEntity nonceEntity = new RequestEntity(HttpMethod.GET, sealURL.getNonceURL());
            ResponseEntity<String> nonceResponse = restTemplate.exchange(nonceEntity, String.class);
            if (nonceResponse.getStatusCode().is2xxSuccessful()) {
                Login login = new Login(nonceResponse.getBody());
                RequestEntity<Login> loginEntity = new RequestEntity(login, HttpMethod.POST, sealURL.login());
                ResponseEntity<Object> loginResponse = restTemplate.exchange(loginEntity, Object.class);
                if (loginResponse.getStatusCode().is2xxSuccessful()) {
                    token = loginResponse.getHeaders().get("X-Session-Token").get(0);
                }
            }
            this.login.put("X-Session-Token", Lists.newArrayList(token));
            lastLoginTime = Instant.now();
        }
        return login;
    }

    public SealContractsResponse getMetadata(String documentId) throws URISyntaxException {
        log.debug("retrieving metadata for document {}", documentId);
        RequestEntity<Login> re = new RequestEntity(login(), HttpMethod.GET, sealURL.getMetadata(documentId));
        ResponseEntity<SealContractsResponse> result = restTemplate.exchange(re, SealContractsResponse.class);
        log.debug(String.format("response received while retrieving metadata for document %s --> %s", documentId, result));
        if (result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        }
        return null;
    }

    public Path getStoredFile(String documentId) throws URISyntaxException, IOException {
        log.debug("retrieving stored file for document {}", documentId);
        final Path tempFile = Files.createTempFile("seal", "source");
        Files.delete(tempFile);

        final MultiValueMap login = login();
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void doWithRequest(ClientHttpRequest request) throws IOException {
                HttpHeaders headers = request.getHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
                String key = (String) login.keySet().iterator().next();
                String token = (String) ((List) login.values().iterator().next()).get(0);
                headers.set(key, token);
            }
        };

        ResponseExtractor<Path> responseExtractor = response -> {
            Files.copy(response.getBody(), tempFile);
            log.debug("Stored file for document {} has been downloaded to {}", documentId, tempFile.toString());
            return tempFile;
        };

        Path returnValue = restTemplate.execute(sealURL.getStoredFile(documentId), HttpMethod.GET, requestCallback, responseExtractor);
        return returnValue;
    }


    public ResponseEntity<Object> updateMetadata(String hash, String fieldName, Object value, Class type) throws URISyntaxException {
        return updateMetadata(hash, fieldName, Lists.newArrayList(value), type);
    }

    public ResponseEntity<Object> updateMetadata(String hash, String fieldName, List<Object> values, Class type) throws URISyntaxException {

        List<Value> body = Lists.newArrayList();
        body.addAll(Value.values(values));

        RequestEntity<List<MetadataItem>> requestEntity =
                new RequestEntity(
                        body,
                        login(),
                        HttpMethod.PUT,
                        sealURL.updateMetadata(hash, fieldName)
                );

        log.debug("updating metadata field {} for document {}", fieldName, hash);
        ResponseEntity<Object> result = restTemplate.exchange(requestEntity, Object.class);
        log.debug(String.format("response received while updating metadata field %s for document %s --> %s", fieldName, hash, result));
        return result;
    }

    @Getter
    private class Login {
        private final String principal;
        private final String password;
        private final String nonce;

        private Login(String nonce) {
            this.principal = configService.getSealUser();
            this.password = configService.getSealPassword();
            this.nonce = nonce;
        }
    }

    @Getter
    private class MetadataItem {

        @JsonIgnore
        private static final String ORIGIN_LABEL = "origin";

        @JsonIgnore
        private static final String IS_NEW_LABEL = "is_new";

        private final List<MetadataAttribute> attributes = Lists.newArrayList();
        private final String origin = "ARIBA";
        private final String type;
        private final Object value;

        private MetadataItem(String type, Object value) {
            this.type = type;
            this.value = value;
            attributes.add(new MetadataAttribute(ORIGIN_LABEL, origin));

        }
    }

    @Getter
    private class MetadataAttribute {

        private final String name;
        private final Object value;

        private MetadataAttribute(String name, Object value) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(value);
            this.name = name;
            this.value = value;
        }
    }


}
