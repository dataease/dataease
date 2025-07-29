package io.dataease.system.dao.auto.mapper;

import io.dataease.system.dao.auto.entity.CoreSysSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface CoreSysSettingRepository extends JpaRepository<CoreSysSetting, Long>, JpaSpecificationExecutor<CoreSysSetting> {

    void deleteByPkey(String pkey);

    Optional<CoreSysSetting> findByPkey(String pkey);

    @Transactional
    default void updateByPkey(String pkey, Integer sort){
        findByPkey(pkey).ifPresent(coreSysSetting -> {
            coreSysSetting.setSort(sort);
            saveAndFlush(coreSysSetting);
        });
    }

    @Transactional
    default void updatePvalByPkey(String pkey, String pval){
        findByPkey(pkey).ifPresent(coreSysSetting -> {
            coreSysSetting.setPval(pval);
            saveAndFlush(coreSysSetting);
        });
    }

}
