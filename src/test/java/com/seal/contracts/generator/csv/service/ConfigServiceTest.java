package com.seal.contracts.generator.csv.service;

import com.seal.contracts.generator.persistence.entity.Config;
import com.seal.contracts.generator.persistence.repository.ConfigRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

/**
 * Created by jantonak on 22/09/17.
 */
public class ConfigServiceTest {

    @Autowired
    private ConfigService configService;

    @Before
    public void setUp() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Context.class);
        ctx.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Test
    public void testGetSupplierFile() throws Exception {
        File supplierFile = configService.getSupplierFile();
        Assert.assertTrue(supplierFile.exists());
    }

    @org.springframework.context.annotation.Configuration
    public static class Context {

        private static Config config = Mockito.mock(Config.class);

        static {
            try {
                Path tempFile = Files.createTempFile("sta", ".tmp");
                Mockito.when(config.getSupplierProfileFile()).thenReturn(tempFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Bean
        public ConfigRepository configRepository() {
            ConfigRepository mock = Mockito.mock(ConfigRepository.class);
            Mockito.when(mock.findOne()).thenReturn(config);
            return mock;
        }

        @Bean
        public ConfigService configService() {
            return new ConfigService();
        }

    }

}