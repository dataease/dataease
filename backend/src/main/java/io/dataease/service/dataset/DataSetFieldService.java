package io.dataease.service.dataset;


import java.util.List;

public interface DataSetFieldService {

    List<Object> fieldValues(String fieldId, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception;

    List<Object> fieldValues(List<String> fieldIds, Long userId, Boolean userPermissions, Boolean needMapping, Boolean rowAndColumnMgm) throws Exception;
}
