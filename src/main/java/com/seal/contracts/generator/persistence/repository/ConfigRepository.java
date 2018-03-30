package com.seal.contracts.generator.persistence.repository;

import com.seal.contracts.generator.persistence.entity.Config;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Created by root on 25.10.15..
 */
@Transactional
@Repository
public interface ConfigRepository extends CrudRepository<Config, String> {
    @Query(value = "SELECT x FROM Config AS x where x.id='0'")
    Config findOne();
}
