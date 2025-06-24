package io.dataease.dataset.dao.auto.mapper;

import io.dataease.dao.auto.entity.CoreDatasetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CoreDatasetGroupRepository extends JpaRepository<CoreDatasetGroup, Long>, JpaSpecificationExecutor<CoreDatasetGroup> {

    List<CoreDatasetGroup> findByNodeType(String nodeType);
}
