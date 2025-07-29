package io.dataease.dao.auto.interceptor;


import io.dataease.utils.EncryptUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EncryptDecryptConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        // 这里实现加密逻辑，示例使用简单 Base64 编码，实际应使用安全加密算法
        return EncryptUtils.aesEncrypt(attribute).toString();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // 这里实现解密逻辑，对应上面的加密算法
        return EncryptUtils.aesDecrypt(dbData).toString();
    }
}
