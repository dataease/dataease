package io.dataease.datasource.dao.auto.repository;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTaskLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;


public interface CoreDatasourceTaskLogRepository extends JpaRepository<CoreDatasourceTaskLog, Long>, JpaSpecificationExecutor<CoreDatasourceTaskLog> {


    @Transactional
    default void deleteByStartTimeLessThan(long threshold){
        Specification<CoreDatasourceTaskLog> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("startTime"), threshold);
        deleteAllInBatch(this.findAll(specification));
    }

}
