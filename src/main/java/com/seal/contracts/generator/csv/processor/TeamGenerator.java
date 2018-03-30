package com.seal.contracts.generator.csv.processor;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.CSVWriter;
import com.seal.contracts.generator.csv.bean.*;
import com.seal.contracts.generator.csv.service.ConfigService;
import com.seal.contracts.generator.csv.service.EnumerationService;
import com.seal.contracts.generator.csv.service.EnumerationServiceImpl;
import com.seal.contracts.generator.csv.service.FolderMappingService;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jantonak on 27/01/17.
 */
@Service
public class TeamGenerator {

    public static final String[] CONTRACT_SHORT_HEADER = new String[]{"Owner", "Title", "ContractId"};

    @Autowired
    private ContractImportItemRepository repository;

    @Autowired
    private ConfigService configService;

    private EnumerationService enumerationService;

    @PostConstruct
    public void init() throws IOException {
        this.enumerationService = new EnumerationServiceImpl(configService.getEnumsFile());
    }
//    public TeamGenerator(ContractImportItemRepository repository, ConfigService configService) throws IOException {
//        this.repository = repository;
//        this.configService = configService;
//        this.enumerationService = new EnumerationServiceImpl(configService.getEnumsFile());
//    }

    public File generate() throws IOException {
        List<ContractImportItem> items = repository.findByStatus(ContractImportItemStatus.ALL_IMPORTED);
        File teamFile = generateTeam(items);
        File contracts = generateContracts(items);
        File documents = generateDocuments();
        File params = generateParameters();
        return zip(teamFile, contracts, documents, params);
    }

    private File generateTeam(Collection<ContractImportItem> items) throws IOException {
        File f = File.createTempFile("ContractTeams_", ".csv");
        List<ContractTeamMember> teamMembers = Lists.newArrayList();
        for (ContractImportItem item : items) {
//            String name = item.getData().getCus_VMOLead();
//
//            Optional<String> idOptional = enumerationService.byDescription(name, EnumerationService.Type.VMOLeadID);
//
//            // Add error handling (if the VMOLead does not exist)
//
//            ContractTeamMember teamMember = new ContractTeamMember();
//            teamMember.setWorkspace(item.getUniqueName());
//            teamMember.setProjectGroup(ContractTeamMember.VMO_LEAD);
//            teamMember.setMember(idOptional.get());

//            teamMembers.add(teamMember);
        }

        CSVWriter<ContractTeamMember> writer = new CSVWriter<ContractTeamMember>(f, ContractTeamMember.class);
        writer.write(teamMembers);
        return f;
    }

    private File generateContracts(Collection<ContractImportItem> items) throws IOException {
        File f = File.createTempFile("Contracts_", ".csv");
        List<Contract> contracts = Lists.newArrayList();
        User owner = configService.getDefaultOwner();
        for (ContractImportItem item : items) {
            Contract contract = new Contract();
            contract.setOwner(owner);
            contract.setContractId(item.getUniqueName());
            contracts.add(contract);
        }
        CSVWriter<Contract> writer = new CSVWriter<Contract>(f, Contract.class, CONTRACT_SHORT_HEADER);
        writer.write(contracts);
        return f;
    }


    private File generateDocuments() throws IOException {
        File f = File.createTempFile("ContractDocuments_", ".csv");
        List<ContractDocument> documents = Lists.newArrayList();
        CSVWriter<ContractDocument> writer = new CSVWriter<ContractDocument>(f, ContractDocument.class);
        writer.write(documents);
        return f;
    }

    private File generateParameters() throws IOException {
        File f = File.createTempFile("ImportProjectsParameters_", ".csv");
        List<ImportProjectParameter> params = Lists.newArrayList(new ImportProjectParameter());
        CSVWriter<ImportProjectParameter> writer = new CSVWriter<ImportProjectParameter>(f, ImportProjectParameter.class);
        writer.write(params);
        return f;
    }

    private File zip(File... files) throws IOException {
        File zipFile = File.createTempFile("TeamMembersUpdate", ".zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (File file : files) {
            FileInputStream in = new FileInputStream(file);
            String entryName = Lists.newArrayList(Splitter.on("_").split(file.getName())).get(0) + ".csv";
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            IOUtils.copy(in, zos);
            in.close();
            zos.closeEntry();
        }
        zos.close();
        return zipFile;
    }


}
