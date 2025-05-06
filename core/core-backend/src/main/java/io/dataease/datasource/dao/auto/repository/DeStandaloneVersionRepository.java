package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.DeStandaloneVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface DeStandaloneVersionRepository extends JpaRepository<DeStandaloneVersion, Long>, JpaSpecificationExecutor<DeStandaloneVersion> {
    @Query("SELECT d FROM DeStandaloneVersion d ORDER BY d.id DESC")
    List<DeStandaloneVersion> findRecords();
}
