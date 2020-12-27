package rsoapp.adms.interceptors;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.MapMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Log4j2
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${spring.application.version}")
    private String applicationVersion;
    @Value("${spring.application.environmentType}")
    private String environmentType;

    @Override
    protected void beforeRequest(HttpServletRequest httpServletRequest, String s) {

        if (!httpServletRequest.getParameterMap().containsKey("uniqueRequestId")) {
            httpServletRequest.setAttribute("uniqueRequestId", UUID.randomUUID().toString());
        }

        Map<String, String> myMap = new HashMap<>();
        myMap.put("applicationName", applicationName);
        myMap.put("applicationVersion", applicationVersion);
        myMap.put("environmentType", environmentType);
        myMap.put("requestUri", httpServletRequest.getRequestURI());
        myMap.put("uniqueRequestId", httpServletRequest.getAttribute("uniqueRequestId").toString());
        myMap.put("method", httpServletRequest.getMethod());
        myMap.put("markerName", "ENTRY");
        log.info(new MapMessage<>(myMap));
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {

        if (!httpServletRequest.getParameterMap().containsKey("uniqueRequestId")) {
            httpServletRequest.setAttribute("uniqueRequestId", UUID.randomUUID().toString());
        }

        Map<String, String> myMap = new HashMap<>();
        myMap.put("applicationName", applicationName);
        myMap.put("applicationVersion", applicationVersion);
        myMap.put("environmentType", environmentType);
        myMap.put("requestUri", httpServletRequest.getRequestURI());
        myMap.put("uniqueRequestId", httpServletRequest.getAttribute("uniqueRequestId").toString());
        myMap.put("method", httpServletRequest.getMethod());
        myMap.put("markerName", "EXIT");
        log.info(new MapMessage<>(myMap));
    }
}
