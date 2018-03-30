package com.seal.contracts.generator.csv.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.seal.contracts.generator.csv.bean.User;
import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import static com.seal.contracts.generator.csv.service.Json.JSON;
import static com.seal.contracts.ws.client.seal.Constants.SEAL_VERSION;
import static com.seal.contracts.ws.client.seal.Constants.SEAL_WS_CHANNEL;

/**
 * Created by jantonak on 24/09/16.
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigRepository repository;

    @Value("${config.file}")
    private File configFile;

    private Config config;

    @PostConstruct
    public Config getConfig() {
        config = repository.findOne();
        if (config == null) {
            try {
                config = initConfig();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    private Config initConfig() throws FileNotFoundException {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Config config = (Config) JSON.toObject(fis, Config.class);
            repository.save(config);
            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getInFile() {
        return new File(config.getInFile());
    }

    public User getDefaultOwner() {
        return new User(config.getDefaultOwner(), Optional.<String>absent());
    }

    public File getUserFile() {
        return new File(config.getUsersFile());
    }

    public File getEnumsFile() {
        return new File(config.getEnumsFile());
    }

    public File getCommodityCodesFile() {
        return new File(config.getCommodityCodesFile());
    }

    public String getContractImportURL() throws MalformedURLException {
        return Joiner.on("/").join(config.getAribaURL(), "ContractWorkspaceImport");
    }

    public String getDocumentsImportURL() throws MalformedURLException {
        return Joiner.on("/").join(config.getAribaURL(), "DocumentImport");
    }

    public String getSealUrl() {
        return Joiner.on("/").join(config.getSealURL(), SEAL_WS_CHANNEL, SEAL_VERSION);
    }

    public String getSealUser() {
        return config.getSealUser();
    }

    public String getSealPassword() {
        return config.getSealPassword();
    }

    public File getSupplierFile() {
        String path = config.getSupplierProfileFile();
        path = path.replace("\\", "/");
        return Paths.get(path).toFile();
    }

}
