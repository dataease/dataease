package io.dataease.xpack.permissions.user.helper;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.apache.commons.collections4.CollectionUtils;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExcelValiHelper {

    public static <T> Map<Integer, String> validateEntity(T obj) throws NoSuchFieldException, SecurityException{
        Map<Integer, String> resultMap = new HashMap<>();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<T>> set = validator.validate(obj, new Class[]{Default.class});
        if (CollectionUtils.isNotEmpty(set)) {
            int i = 0;

            for (ConstraintViolation<T> cv : set) {
                Field declaredField = obj.getClass().getDeclaredField(cv.getPropertyPath().toString());
                ExcelProperty annotation = declaredField.<ExcelProperty>getAnnotation(ExcelProperty.class);
                int index = annotation.index();
                if (index == -1) {
                    index = i;
                }
                resultMap.put(Integer.valueOf(index), cv.getMessage());
                i++;
            }
        }
        return (resultMap.size() == 0) ? null : resultMap;
    }
}
