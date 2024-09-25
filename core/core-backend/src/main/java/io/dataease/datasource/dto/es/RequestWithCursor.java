package io.dataease.datasource.dto.es;

import lombok.Data;

@Data
public class RequestWithCursor extends Request {
    private String cursor;
}
