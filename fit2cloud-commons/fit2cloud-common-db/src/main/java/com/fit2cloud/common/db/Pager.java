package com.fit2cloud.common.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pager<T> {

    private T listObject;

    private long itemCount;

    private long pageCount;


}
