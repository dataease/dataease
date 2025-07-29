package io.dataease.system.dao.auto.mapper;

import io.dataease.system.dao.auto.entity.CoreTypeface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreTypefaceRepository extends JpaRepository<CoreTypeface, Long>, JpaSpecificationExecutor<CoreTypeface> {


}
