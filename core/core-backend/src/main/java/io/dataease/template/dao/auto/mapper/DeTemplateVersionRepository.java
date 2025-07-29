package io.dataease.template.dao.auto.mapper;

import io.dataease.template.dao.auto.entity.DeTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface DeTemplateVersionRepository extends JpaRepository<DeTemplateVersion, Long>, JpaSpecificationExecutor<DeTemplateVersion> {


}
