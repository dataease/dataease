package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreDeEngineRepository extends JpaRepository<CoreDeEngine, Long>, JpaSpecificationExecutor<CoreDeEngine> {

}
