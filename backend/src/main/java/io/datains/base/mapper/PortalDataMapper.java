package io.datains.base.mapper;


import io.datains.base.domain.PortalData;

import java.util.List;

/**
 * <p>
 * 数据门户 Mapper 接口
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-06-02
 */
public interface PortalDataMapper {

     List<PortalData> list(Long userId);

    Integer listCount(Long userId);

     void save(PortalData portalData);


     void del(Integer id);


     void update(PortalData portalData);

}
