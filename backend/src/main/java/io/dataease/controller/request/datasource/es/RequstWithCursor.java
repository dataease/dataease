package io.dataease.controller.request.datasource.es;

import lombok.Data;

@Data
public class RequstWithCursor extends Requst{
    private String cursor;
}
