package io.dataease.menu.dao.auto.mapper;


import io.dataease.menu.dao.auto.entity.CoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CoreMenuRepository extends JpaRepository<CoreMenu, Long>, JpaSpecificationExecutor<CoreMenu> {


}
