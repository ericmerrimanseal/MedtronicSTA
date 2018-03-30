package com.seal.contracts.generator.ui;

import com.seal.contracts.generator.file.DataFolderInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by root on 17.08.15..
 */

@SpringBootApplication
@ComponentScan(basePackages = "com.seal.contracts")
@EntityScan("com.seal.contracts.generator.persistence.entity")
@Configuration
@EnableJpaRepositories(basePackages = "com.seal.contracts.generator.persistence")
@EnableScheduling
public class Application {

    public static void main(String[] args) throws IOException, URISyntaxException {
        new DataFolderInitializer().init();
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths("com.seal.contracts.ariba.wsdl.contractworkspaceimport", "com.seal.contracts.ariba.wsdl.document");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() throws Exception {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller());
        webServiceTemplate.setUnmarshaller(marshaller());
        return webServiceTemplate;
    }

    @Bean
    public File inFolder() {
        return new File("in");
    }
}
