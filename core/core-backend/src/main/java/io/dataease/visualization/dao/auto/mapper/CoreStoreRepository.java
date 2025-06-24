package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.CoreStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;


public interface CoreStoreRepository extends JpaRepository<CoreStore, Long>, JpaSpecificationExecutor<CoreStore> {
    @Query("SELECT cs.resourceId FROM CoreStore cs WHERE cs.uid = :uid")
    Set<Long> findStoredResourceIdsByUser(Long uid);
}
