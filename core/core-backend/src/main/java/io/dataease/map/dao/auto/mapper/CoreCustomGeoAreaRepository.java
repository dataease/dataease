package io.dataease.map.dao.auto.mapper;

import io.dataease.map.dao.auto.entity.CoreCustomGeoArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreCustomGeoAreaRepository extends JpaRepository<CoreCustomGeoArea, String>, JpaSpecificationExecutor<CoreCustomGeoArea> {


}
