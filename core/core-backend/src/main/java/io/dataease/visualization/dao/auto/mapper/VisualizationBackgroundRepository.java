package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;


public interface VisualizationBackgroundRepository extends JpaRepository<VisualizationBackground, String>, JpaSpecificationExecutor<VisualizationBackground> {

    @Transactional
    default void updateNameById(String id, String name){
        VisualizationBackground background = findById(id).orElse(null);
        if (background != null) {
            background.setName(name);
            saveAndFlush(background);
        }
    }

}
