package io.dataease.font.dao.auto.mapper;

import io.dataease.font.dao.auto.entity.CoreFont;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface CoreFontRepository extends JpaRepository<CoreFont, Long>, JpaSpecificationExecutor<CoreFont> {
    List<CoreFont> findByName(String name);

    List<CoreFont> findByFileTransName(String fileTransName);

    List<CoreFont> findByisDefault(Boolean isDefault);

    @Transactional
    default void updateIsDefaultById(Long id, Boolean isDefault) {
        this.findById(id).map(coreFont -> {
            coreFont.setIsDefault(isDefault);
            this.saveAndFlush(coreFont);
            return 1;
        });
    }

    @Transactional
    default void resetDefaultById(Long id) {
        Specification<CoreFont> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("id"), id);
        List<CoreFont> coreFonts = this.findAll(spec);
        for (CoreFont coreFont : coreFonts) {
            coreFont.setIsDefault(false);
            this.saveAndFlush(coreFont);
        }
    }
}
