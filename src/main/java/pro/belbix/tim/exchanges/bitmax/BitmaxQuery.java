package pro.belbix.tim.exchanges.bitmax;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.rest.Request;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SuppressWarnings("Duplicates")
@Getter
@Setter
public class BitmaxQuery<T extends Request, N> {
    private String apiKey;
    private String apiSecret;
    private String fullUrl;
    private T requestModel;
    private Class<N> responseClass;
    private HttpMethod httpMethod;
    private String body = "";
    private Boolean useAuth = true;
    private String group;
    private String coid;
    private Long timestamp = null;

    private Mac hmac;
    private byte[] hmacKey;
    private SecretKeySpec keySpec;

    public HttpEntity<T> buildHttpEntity() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            body = mapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            throw new TIMRuntimeException("Error parse request", e);
        }
        validate();
        return new HttpEntity<>(requestModel, buildHeaders());
    }

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (!useAuth) return headers;
        validate();
        if (group != null && !group.isBlank() && !fullUrl.contains("/" + group + "/"))
            fullUrl = fullUrl.replace("/api", "/" + group + "/api");

        try {
            hmacKey = Base64.getDecoder().decode(apiSecret);
            hmac = Mac.getInstance("HmacSHA256");
            keySpec = new SecretKeySpec(hmacKey, "HmacSHA256");
            hmac.init(keySpec);
            String url = getPathFromUrl(fullUrl);
            if (url.contains("v1/")) {
                url = url.split("v1/")[1];
            } else if (url.contains("v2/")) {
                url = url.split("v2/")[1];
            } else {
                throw new TIMRuntimeException("Unknown API version");
            }
            if (url.contains("?")) url = url.split("\\?")[0];

            long timestamp;
            if (this.timestamp == null) {
                timestamp = System.currentTimeMillis();
            } else {
                timestamp = this.timestamp;
            }

            headers.add("x-auth-key", apiKey);
            headers.add("x-auth-signature", generateSig(url, timestamp));
            headers.add("x-auth-timestamp", timestamp + "");

            if (coid != null && !coid.isBlank()) {
                headers.add("x-auth-coid", coid);
            }
        } catch (Exception e) {
            throw new TIMRuntimeException("Generate Signature error", e);
        }
        return headers;
    }

    private String getPathFromUrl(String fullUrl) throws MalformedURLException {
        URL u = new URL(fullUrl);
        String path;
        if (u.getQuery() != null && !u.getQuery().isBlank()) {
            path = u.getPath() + "?" + u.getQuery();
        } else {
            path = u.getPath();
        }
        return path;
    }

    private void validate() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new TIMRuntimeException("Invalid apiKey");
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new TIMRuntimeException("Invalid apiSecret");
        }
        if (fullUrl == null || fullUrl.isBlank()) {
            throw new TIMRuntimeException("Invalid fullUrl");
        }
        if (httpMethod == null) {
            throw new TIMRuntimeException("Invalid httpMethod");
        }
        if (responseClass == null) {
            throw new TIMRuntimeException("Invalid responseClass");
        }

        if (httpMethod.equals(HttpMethod.POST) && requestModel == null) {
            throw new TIMRuntimeException("Invalid requestModel");
        }
        if (httpMethod.equals(HttpMethod.POST) && (body == null || body.isBlank())) {
            throw new TIMRuntimeException("Invalid body: " + body);
        }
    }

    private String generateSig(String url, long timestamp) {
        String prehash = timestamp + "+" + url;
        if (coid != null && !coid.isBlank()) {
            prehash += "+" + coid;
        }
        byte[] encoded = Base64.getEncoder().encode(hmac.doFinal(prehash.getBytes(StandardCharsets.UTF_8)));
        return new String(encoded);
    }

    @Override
    public String toString() {
        return "BitmaxQuery{" +
                (fullUrl != null ? "fullUrl='" + fullUrl + '\'' + "," : "") +
                (requestModel != null ? "requestModel=" + requestModel + "," : "") +
                (responseClass != null ? "responseClass=" + responseClass + "," : "") +
                (httpMethod != null ? "httpMethod=" + httpMethod + "," : "") +
                (body != null ? "body='" + body + '\'' + "," : "") +
                (useAuth != null ? "useAuth=" + useAuth + "," : "") +
                (group != null ? "group='" + group + '\'' + "," : "") +
                (coid != null ? "coid='" + coid + '\'' + "," : "") +
                '}';
    }
}
