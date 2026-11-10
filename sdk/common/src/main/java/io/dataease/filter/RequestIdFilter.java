package io.dataease.filter;

import io.dataease.exception.ErrorResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilterAsyncDispatch() { return false; }

    @Override
    protected boolean shouldNotFilterErrorDispatch() { return false; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String supplied = request.getAttribute(ErrorResponses.REQUEST_ID) instanceof String existing
                ? existing : request.getHeader(ErrorResponses.REQUEST_ID);
        String id = supplied != null && supplied.matches("[a-fA-F0-9]{32}")
                ? supplied : UUID.randomUUID().toString().replace("-", "");
        request.setAttribute(ErrorResponses.REQUEST_ID, id);
        response.setHeader(ErrorResponses.REQUEST_ID, id);
        if (ErrorResponses.modern(request)) response.setHeader(ErrorResponses.CONTRACT, "2");
        String previous = MDC.get("requestId");
        MDC.put("requestId", id);
        try {
            chain.doFilter(request, response);
        } finally {
            if (previous == null) MDC.remove("requestId");
            else MDC.put("requestId", previous);
        }
    }
}
