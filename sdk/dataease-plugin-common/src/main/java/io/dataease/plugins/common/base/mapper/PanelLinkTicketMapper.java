package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelLinkTicket;
import io.dataease.plugins.common.base.domain.PanelLinkTicketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelLinkTicketMapper {
    long countByExample(PanelLinkTicketExample example);

    int deleteByExample(PanelLinkTicketExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PanelLinkTicket record);

    int insertSelective(PanelLinkTicket record);

    List<PanelLinkTicket> selectByExample(PanelLinkTicketExample example);

    PanelLinkTicket selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PanelLinkTicket record, @Param("example") PanelLinkTicketExample example);

    int updateByExample(@Param("record") PanelLinkTicket record, @Param("example") PanelLinkTicketExample example);

    int updateByPrimaryKeySelective(PanelLinkTicket record);

    int updateByPrimaryKey(PanelLinkTicket record);
}