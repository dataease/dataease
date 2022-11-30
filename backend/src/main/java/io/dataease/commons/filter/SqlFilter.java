package io.dataease.commons.filter;

import io.dataease.commons.exception.DEException;
import io.dataease.commons.holder.ThreadLocalContextHolder;
import io.dataease.commons.wrapper.XssAndSqlHttpServletRequestWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class SqlFilter implements Filter {

    private List<String> excludedUris = new ArrayList<>();

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (ObjectUtils.isEmpty(RequestContextHolder.getRequestAttributes())) {
            ServletRequestAttributes attributes = new ServletRequestAttributes((HttpServletRequest) request);
            RequestContextHolder.setRequestAttributes(attributes);
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if ("TRACE".equalsIgnoreCase(httpRequest.getMethod()) || "TRACK".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if(excludedUris.contains(((HttpServletRequest) request).getRequestURI())){
            chain.doFilter(request, response);
        }else {
            String method = "GET";
            String param;
            XssAndSqlHttpServletRequestWrapper xssRequest = null;
            if (request instanceof HttpServletRequest) {
                method = ((HttpServletRequest) request).getMethod();
                xssRequest = new XssAndSqlHttpServletRequestWrapper((HttpServletRequest) request);
            }
            if ("POST".equalsIgnoreCase(method)) {
                param = this.getBodyString(xssRequest.getReader());
                if (StringUtils.isNotBlank(param)) {
                    if (xssRequest.checkXSSAndSql(param)) {
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json;charset=UTF-8");
                        String msg = ThreadLocalContextHolder.getData().toString();
                        DEException.throwException(msg);
                        return;
                    }
                }
            }
            if (xssRequest.checkParameter()) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                String msg = ThreadLocalContextHolder.getData().toString();
                DEException.throwException(msg);
                return;
            }
            chain.doFilter(xssRequest, response);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        excludedUris.add("/dataset/table/excel/upload");
    }

    // 获取request请求body中参数
    public static String getBodyString(BufferedReader br) {
        String inputLine;
        String str = "";
        try {
            while ((inputLine = br.readLine()) != null) {
                str += inputLine;
            }
            br.close();
        } catch (IOException e) {
        }
        return str;

    }


}
