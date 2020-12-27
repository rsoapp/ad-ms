package rsoapp.adms.interceptors;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.message.MapMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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

        Map<String, String> myMap = new HashMap<>();
        myMap.put("applicationName", applicationName);
        myMap.put("applicationVersion", applicationVersion);
        myMap.put("environmentType", environmentType);
        log.info(new MapMessage<>(myMap));
    }

    @Override
    protected void afterRequest(HttpServletRequest httpServletRequest, String s) {

        Map<String, String> myMap = new HashMap<>();
        myMap.put("applicationName", applicationName);
        myMap.put("applicationVersion", applicationVersion);
        myMap.put("environmentType", environmentType);
        log.info(new MapMessage<>(myMap));
    }
}
