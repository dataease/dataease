package io.dataease.provider.query.api;

import io.dataease.provider.QueryProviderImpl;
import org.springframework.stereotype.Service;

@Service("apiQuery")
public class ApiProvider extends QueryProviderImpl {
    @Override
    public Integer transFieldType(String field) {
        switch (field) {
            case "0":
                return 0;// 文本
            case "1":
                return 1;// 时间
            case "2":
                return 2;// 整型
            case "3":
                return 3;// 浮点
            case "4":
                return 4;// 布尔
            default:
                return 0;
        }
    }
}
