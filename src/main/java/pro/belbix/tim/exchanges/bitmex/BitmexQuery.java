package pro.belbix.tim.exchanges.bitmex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
public class BitmexQuery<T extends Request, N> {
    private String apiKey;
    private String apiSecret;
    private String fullUrl;
    private T requestModel;
    private Class<N> responseClass;
    private HttpMethod httpMethod;
    private String body = "";
    private Boolean useAuth = true;

    public static String generateSignature(String apiSecret, String method, String fullUrl, String expires, String body)
            throws MalformedURLException, InvalidKeyException, NoSuchAlgorithmException {
        if (body == null) {
            body = "";
        }
        URL u = new URL(fullUrl);
        String path;
        if (u.getQuery() != null && !u.getQuery().isBlank()) {
            path = u.getPath() + "?" + u.getQuery();
        } else {
            path = u.getPath();
        }
        return encode(apiSecret, method + path + expires + body);
    }

    private static String encode(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public HttpEntity<T> buildHttpEntity() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            body = mapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            throw new TIMRuntimeException("Error parse request", e);
        }
        validate();
        return new HttpEntity<>(requestModel, buildHeaders());
    }

    public HttpHeaders buildHeaders() {
        if (!useAuth) return new HttpHeaders();
        validate();
        String expires = "" + (LocalDateTime.now().plus(2, ChronoUnit.MINUTES).toEpochSecond(ZoneOffset.UTC));
        String signature;
        try {
            signature = generateSignature(apiSecret, httpMethod.name(), fullUrl, expires, body);
        } catch (MalformedURLException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new TIMRuntimeException("Generate Signature error", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-expires", expires);
        headers.add("api-key", apiKey);
        headers.add("api-signature", signature);
        return headers;
    }

    private boolean validate() {
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
        return true;
    }
}
