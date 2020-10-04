package pro.belbix.tim.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.belbix.tim.exchanges.bitmex.BitmexREST;
import pro.belbix.tim.properties.RestProperties;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestService {
    private static final Logger log = LoggerFactory.getLogger(BitmexREST.class);
    private final RestTemplate REST_TEMPLATE;
    private final RestProperties prop;

    @Autowired
    public RestService(RestProperties prop) {
        this.prop = prop;
        REST_TEMPLATE = restTemplate();
    }

    public <T, N> ResponseEntity<T> post(String url,
                                         HttpEntity<N> httpEntity,
                                         Class<T> responseClass) {
        log.debug("New post request for " + url);
        return REST_TEMPLATE.exchange(url, HttpMethod.POST, httpEntity, responseClass);
    }

    public <T, N> ResponseEntity<T> put(String url,
                                        HttpEntity<N> httpEntity,
                                        Class<T> responseClass) {
        log.debug("New put request for " + url);
        return REST_TEMPLATE.exchange(url, HttpMethod.PUT, httpEntity, responseClass);
    }

    public <T> ResponseEntity<T> get(String url,
                                     HttpEntity<?> httpEntity,
                                     Class<T> responseClass) {
        log.debug("New get request for " + url);
        return REST_TEMPLATE.exchange(url, HttpMethod.GET, httpEntity, responseClass);
    }

    public <T, N> ResponseEntity<T> delete(String url,
                                           HttpEntity<N> httpEntity,
                                           Class<T> responseClass) {
        log.debug("New delete request for " + url);
        return REST_TEMPLATE.exchange(url, HttpMethod.DELETE, httpEntity, responseClass);
    }

    private RestTemplate restTemplate() {
        if (prop.isLoggingRequest()) {
            RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new LoggingRequestInterceptor());
            restTemplate.setInterceptors(interceptors);
            return restTemplate;
        } else {
            return new RestTemplate();
        }
    }

}
