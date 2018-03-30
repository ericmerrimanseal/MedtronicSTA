package com.seal.contracts.generator.persistence.repository;

import com.seal.contracts.generator.persistence.entity.ContractImportDocument;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportDocumentStatus;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by root on 25.10.15..
 */
@Transactional
@Repository
public interface ContractImportDocumentRepository extends CrudRepository<ContractImportDocument, String> {

    @Query(value = "SELECT STATUS,COUNT(STATUS) FROM CONTRACT_IMPORT_DOCUMENT GROUP BY STATUS", nativeQuery = true)
    List<Object[]> groupByStatus();

    Iterable<ContractImportDocument> findByStatus(ContractImportDocumentStatus status);
}
