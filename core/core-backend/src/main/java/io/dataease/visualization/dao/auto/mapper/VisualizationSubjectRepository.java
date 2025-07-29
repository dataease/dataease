package io.dataease.visualization.dao.auto.mapper;

import io.dataease.visualization.dao.auto.entity.VisualizationSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;


public interface VisualizationSubjectRepository extends JpaRepository<VisualizationSubject, String>, JpaSpecificationExecutor<VisualizationSubject> {

    @Transactional
    default void updateNameById(String id, String name){
        findById(id).ifPresent(visualizationSubject -> {
            visualizationSubject.setName(name);
            saveAndFlush(visualizationSubject);
        });
    }

}
