package com.seal.contracts.generator.persistence.repository;

import com.seal.contracts.generator.csv.bean.Contract;
import com.seal.contracts.generator.persistence.entity.ContractImportItem;
import com.seal.contracts.generator.persistence.entity.enums.ContractImportItemStatus;
import com.seal.contracts.generator.persistence.entity.enums.SealSyncStatus;
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
public interface ContractImportItemRepository extends CrudRepository<ContractImportItem, String> {
    List<ContractImportItem> findByStatus(ContractImportItemStatus status);

    List<ContractImportItem> findBySealSyncStatus(SealSyncStatus sealSyncStatus);

    List<ContractImportItem> findByHierarchicalType(Contract.HIERARCHICAL_TYPE type);

    @Query(value = "SELECT HIERARCHICALTYPE,COUNT(HIERARCHICALTYPE) FROM CONTRACT_IMPORT_ITEM GROUP BY HIERARCHICALTYPE", nativeQuery = true)
    List<Object[]> groupByHierarchicalType();

}
