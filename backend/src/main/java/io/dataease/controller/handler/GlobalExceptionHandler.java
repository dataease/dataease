package io.dataease.controller.handler;

import io.dataease.controller.ResultHolder;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;


@RestController
public class GlobalExceptionHandler implements ErrorController {

    public static final org.slf4j.Logger Logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String PATH = "/error";

    @Resource
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(value = {PATH}, produces = {"text/html"})
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView("error", this.getErrorAttributes(request, false));
    }

    @RequestMapping(value = PATH)
    public ResultHolder error(HttpServletRequest request, HttpServletResponse response) {
        WebRequest webRequest = new ServletWebRequest(request);
        Throwable t = errorAttributes.getError(webRequest);
        Map<String, Object> errorAttributeMap = errorAttributes.getErrorAttributes(webRequest, true);
        Integer code = (Integer) errorAttributeMap.get("status");
        response.setStatus(code);
        String errorMessage = StringUtils.EMPTY;
        if (t != null) {
            if (Logger.isDebugEnabled()) {
                Logger.error("Fail to proceed " + errorAttributeMap.get("path"), t);
            }
            errorMessage = t.getMessage();
        }
        if (StringUtils.isBlank(errorMessage)) {
            if (code == 403) {
                errorMessage = "Permission Denied.";
            } else if (code == 404) {
                String path = request.getServletPath();
                if (Objects.nonNull(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)) && StringUtils.isNotBlank(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString())) {
                    path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString();
                }
                errorMessage = path + " not found.";
            } else {
                errorMessage = "The server responds " + code + " but no detailed message.";
            }
        }

        return ResultHolder.error(Translator.get(errorMessage));
    }

    protected Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        WebRequest webRequest = new ServletWebRequest(request);
        return this.errorAttributes.getErrorAttributes(webRequest, includeStackTrace);
    }

}
