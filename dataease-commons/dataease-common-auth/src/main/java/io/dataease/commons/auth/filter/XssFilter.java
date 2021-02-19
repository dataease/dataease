package io.dataease.commons.auth.filter;


import cn.hutool.core.util.StrUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Component
@WebFilter
public class XssFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        XssHttpServletRequestWrapper xssHttpServletRequestWrapper = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        filterChain.doFilter(xssHttpServletRequestWrapper, response);
    }

    @Override
    public void destroy() {

    }

    class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private HttpServletRequest request;

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getParameter(String name) {
            String oldValue = super.getParameter(name);
            if(StrUtil.isEmpty(oldValue)){
                return oldValue;
            }
            String newValue = StringEscapeUtils.escapeHtml4(oldValue);
            return newValue;
        }

    }
}
