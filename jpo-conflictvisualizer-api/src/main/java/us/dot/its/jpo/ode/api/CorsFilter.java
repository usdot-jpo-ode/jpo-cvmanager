package us.dot.its.jpo.ode.api;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Custom servlet filter to add CORS header
 */
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var response = (HttpServletResponse)servletResponse;
        var request = (HttpServletRequest)servletRequest;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET,POST,DELETE");
        response.setHeader("Access-Control-Allow-Headers", "authorization");
        response.setIntHeader("Access-Control-Max-Age", 1800);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }
}
