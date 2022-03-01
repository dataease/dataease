package io.dataease.controller.request.datasource.es;

import lombok.Data;

@Data
public class RequestWithCursor extends Request {
    private String cursor;
}
