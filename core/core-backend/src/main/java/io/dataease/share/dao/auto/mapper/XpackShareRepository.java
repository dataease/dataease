package io.dataease.share.dao.auto.mapper;

import io.dataease.share.dao.auto.entity.XpackShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface XpackShareRepository extends JpaRepository<XpackShare, Long>, JpaSpecificationExecutor<XpackShare> {



}
