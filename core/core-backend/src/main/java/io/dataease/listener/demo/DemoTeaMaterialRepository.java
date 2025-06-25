package io.dataease.listener.demo;

import io.dataease.dao.auto.entity.CoreDatasetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DemoTeaMaterialRepository extends JpaRepository<DemoTeaMaterial, Integer>, JpaSpecificationExecutor<DemoTeaMaterial> {


}
