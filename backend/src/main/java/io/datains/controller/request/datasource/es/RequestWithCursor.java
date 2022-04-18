package io.datains.controller.request.datasource.es;

import lombok.Data;

@Data
public class RequestWithCursor extends Request {
    private String cursor;
}
