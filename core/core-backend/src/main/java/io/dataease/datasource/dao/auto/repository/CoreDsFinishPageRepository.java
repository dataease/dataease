package io.dataease.datasource.dao.auto.repository;


import io.dataease.datasource.dao.auto.entity.CoreDsFinishPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreDsFinishPageRepository extends JpaRepository<CoreDsFinishPage, Long>, JpaSpecificationExecutor<CoreDsFinishPage> {

}
