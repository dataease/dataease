package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.CoreStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreStoreRepository extends JpaRepository<CoreStore, Long>, JpaSpecificationExecutor<CoreStore> {

}
