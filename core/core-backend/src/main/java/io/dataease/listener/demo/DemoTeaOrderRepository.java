package io.dataease.listener.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemoTeaOrderRepository extends JpaRepository<DemoTeaOrder, Integer>, JpaSpecificationExecutor<DemoTeaOrder> {


}
