package io.dataease.service.dataset;




import io.dataease.dto.dataset.DeSortDTO;

import java.util.List;

public interface DataSetFieldService {

    List<Object> fieldValues(String fieldId, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception;

    List<Object> fieldValues(String fieldId, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception;

    List<Object> fieldValues(List<String> fieldIds, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean needMapping, Boolean rowAndColumnMgm) throws Exception;
}
