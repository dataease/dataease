package io.dataease.service.dataset;


import java.util.List;

public interface DataSetFieldService {

    List<Object> fieldValues(String fieldId, Long userId, Boolean userPermissions) throws Exception;
}
