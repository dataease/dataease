package io.dataease.commons.base;

import cn.hutool.core.util.StrUtil;
import io.dataease.commons.base.result.ResultEntity;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.*;
import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class Fit2cloudException extends ResultEntity implements ErrorController {
    public static final org.slf4j.Logger Logger = LoggerFactory.getLogger(Fit2cloudException.class);
    private static final String PATH = "/error";


    @Resource
    private ErrorAttributes errorAttributes;
    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(PATH)
    public ResultEntity error(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        WebRequest webRequest = new ServletWebRequest(request);
        ErrorAttributeOptions.Include[] values = ErrorAttributeOptions.Include.values();
        List<ErrorAttributeOptions.Include> collect = Stream.of(values).collect(Collectors.toList());
        ErrorAttributeOptions options = ErrorAttributeOptions.of(collect);
        Map<String, Object> errorAttributeMap = this.errorAttributes.getErrorAttributes(webRequest, options);
        Throwable t = errorAttributes.getError(webRequest);
        Integer code = (Integer) errorAttributeMap.get("status");
        response.setStatus(code);
        String errorMessage = StrUtil.EMPTY;
        if (t != null) {
            if (Logger.isDebugEnabled()) {
                Logger.error("Fail to proceed " + errorAttributeMap.get("path"), t);
            }
            errorMessage = t.getMessage();
        }
        if (StrUtil.isBlank(errorMessage)) {
            if (code == 403) {
                errorMessage = "Permission Denied.";
            } else if (code == 404) {
                String path = request.getServletPath();
                if(Objects.nonNull(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) && StrUtil.isNotBlank(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString())){
                    path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString();
                }
                errorMessage = path + " not found.";
            } else {
                errorMessage = "The server responds " + code + " but no detailed message.";
            }
        }
        return error(errorMessage);

    }
}
