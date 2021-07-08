package io.dataease.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/7/8 3:12 下午
 */
public class SQLConstants {
    public static final List<Integer> DIMENSION_TYPE = new ArrayList<Integer>() {{
        add(0);// 文本
        add(1);// 时间
        add(5);// 地理位置
    }};
}
