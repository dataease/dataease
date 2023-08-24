package io.dataease.plugins.xpack.email.dto.request;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.HtmlUtils;

import lombok.Data;

@Data
public class XpackEmailCreate implements Serializable{

    private XpackEmailTaskRequest request;

    private String emailContent;


    public XpackEmailTaskRequest fillContent() {
        if (StringUtils.isBlank(emailContent)) {
            return request;
        }
        String htmlEscape = HtmlUtils.htmlEscape(emailContent);

        byte[] bytes;
        try {
            bytes = htmlEscape.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        request.setContent(bytes);
        return request;
    }
    
}
