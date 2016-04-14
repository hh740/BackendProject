package com.codelab.web.security;

import com.codelab.web.constants.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;


public class ReqAnalysisFilter implements Filter {

    public static final String SPACE = " ";

    private static Logger logger = LoggerFactory.getLogger(ReqAnalysisFilter.class);

    private AtomicInteger offset = new AtomicInteger(0);

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        boolean e = false;
        long start = System.currentTimeMillis();
        try {
            offset.incrementAndGet();
            chain.doFilter(req, res);
        } catch (Throwable t) {
            e = true;
            if(t instanceof IOException) throw (IOException)t;
            if(t instanceof ServletException) throw (ServletException)t;
            throw new ServletException(t);
        } finally {
            offset.decrementAndGet();
            long end = System.currentTimeMillis();
            long time = end - start;
            if(time <= 100) {
                logger.info("{} uri:{}, method:{}, time:{}ms, offset:{}, e:{}", Config.LOGTAG_REQSTSN, uri, method, time, offset, e);
            }else{
                logger.warn("{} uri:{}, method:{}, time:{}ms, offset:{}, e:{}, url:{}", Config.LOGTAG_REQSTSA, uri, method, time, offset, e, getFullUrl(request));
            }
        }
    }

    private String getFullUrl(HttpServletRequest request){
        StringBuffer sb = request.getRequestURL();
        String query = request.getQueryString();
        if(StringUtils.isNotBlank(query)) sb.append("?").append(query);
        return sb.toString();
    }

    private String getMainReqInfo(HttpServletRequest request) {
        String schema = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        StringBuilder sb = new StringBuilder();
        sb.append("REQINFO").append(SPACE);
        sb.append(schema).append(SPACE);
        sb.append(serverName).append(SPACE);
        sb.append(port).append(SPACE);
        sb.append(method).append(SPACE);
        sb.append(uri).append(SPACE);
        @SuppressWarnings("unchecked")
        Enumeration<String> parameters = request.getParameterNames();
        int i = 0;
        while (parameters.hasMoreElements()) {
            if (i > 0) sb.append("&");
            i++;
            String name = parameters.nextElement();
            String value = request.getParameter(name);
            sb.append(name).append("=").append(value);
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void logHttpInfo(HttpServletRequest request){
        if(!logger.isDebugEnabled()) return;
        String method = request.getMethod();
        logger.info("Http method:{}", method);
        Enumeration<String> headers = request.getHeaderNames();
        while(headers.hasMoreElements()){
            String name = headers.nextElement();
            String value = request.getHeader(name);
            logger.debug("Http header:{} = {}", name, value);
        }
        Enumeration<String> parameters = request.getParameterNames();
        while(parameters.hasMoreElements()){
            String name = parameters.nextElement();
            String value = request.getParameter(name);
            logger.debug("Http parameter:{} = {}", name, value);
        }
    }

    @Override
    public void destroy() {

    }

}
