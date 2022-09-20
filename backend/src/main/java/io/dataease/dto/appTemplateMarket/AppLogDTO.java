package io.dataease.dto.appTemplateMarket;

import io.dataease.dto.log.FolderItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AppLogDTO implements Serializable {

    private Integer sourceType;

    private String sourceId;

    private String sourceName;

    private Integer operateType;

    private List<FolderItem> positions;

    private List<FolderItem> remarks;

}
