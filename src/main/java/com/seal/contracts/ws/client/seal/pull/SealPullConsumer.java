package com.seal.contracts.ws.client.seal.pull;

import com.seal.contracts.generator.csv.bean.BuildResult;
import com.seal.contracts.generator.csv.bean.Export;
import com.seal.contracts.generator.csv.mapper.ExportToContractMapper;
import com.seal.contracts.generator.csv.processor.ExportProcessor;
import com.seal.contracts.generator.csv.service.*;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.repository.ContractImportDocumentRepository;
import com.seal.contracts.generator.persistence.repository.ContractImportItemRepository;
import com.seal.contracts.threading.DataConsumer;
import com.seal.contracts.threading.DataProducer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by jantonak on 19/07/17.
 */
@Slf4j
public class SealPullConsumer extends DataConsumer<SimpleExports, ContractImportItem> {

    private final ConfigService configService;
    private final FolderMappingService folderMappingService;
    private final ContractImportItemRepository itemsRepository;
    private final ContractImportDocumentRepository documentsRepository;
    private final UsersService usersService;
    private final SupplierService supplierService;
    private final EnumerationService enumerationService;
    private final CommodityCodeService commodityCodeService;
    private final ExportService exportService;
    private final ContractImportDocumentRepository documentRepository;


    protected SealPullConsumer(DataProducer<SimpleExports, ContractImportItem> producer, ConfigService configService, FolderMappingService folderMappingService, ContractImportItemRepository itemsRepository, ContractImportDocumentRepository documentsRepository, UsersService usersService, SupplierService supplierService, EnumerationService enumerationService, CommodityCodeService commodityCodeService, ExportService exportService, ContractImportDocumentRepository documentRepository) {
        super(producer);
        this.configService = configService;
        this.folderMappingService = folderMappingService;
        this.itemsRepository = itemsRepository;
        this.documentsRepository = documentsRepository;
        this.usersService = usersService;
        this.supplierService = supplierService;
        this.enumerationService = enumerationService;
        this.commodityCodeService = commodityCodeService;
        this.exportService = exportService;
        this.documentRepository = documentRepository;
    }

    @Override
    public ContractImportItem process(SimpleExports item) throws Exception {

        log.info("Processing masterdata for {}", item.getLegacyContractId());

        BuildResult.BuildResultBuilder resultBuilder = BuildResult.newBuilder(configService.getConfig(), BuildResult.STATUS.IN_PROGRESS);

        final ExportToContractMapper contractMapper = new ExportToContractMapper(usersService, supplierService, configService.getConfig(), enumerationService, commodityCodeService, exportService, itemsRepository, folderMappingService, documentRepository);
        ExportProcessor exportProcessor = new ExportProcessor(contractMapper, item.getLegacyContractId(), itemsRepository, documentsRepository, resultBuilder, true, folderMappingService);

        List<Export> exports = item.getExports();
//        for (Export export : exports) {
//            export.setCommodity("unspsc:All");
//            export.setPaymentTerms("Within 30 Days Due Net");
//            export.setSupplier("ACM_4174615");
//        }

        ContractImportItem importedItem = exportProcessor.process(exports);
        return importedItem;

    }
}
