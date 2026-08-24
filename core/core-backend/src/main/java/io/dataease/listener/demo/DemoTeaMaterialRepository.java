package io.dataease.listener.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemoTeaMaterialRepository extends JpaRepository<DemoTeaMaterial, Integer>, JpaSpecificationExecutor<DemoTeaMaterial> {


}
