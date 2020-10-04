package pro.belbix.tim.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.exchanges.bitmex.dto.TradeResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class RestServiceTest {
    private static final String BITMEX_URL = "https://www.bitmex.com/api/v1/trade?symbol=XBTUSD&count=500&startTime=2020-09-20T18:01:45.159";
    private static final String BITMEX_URL2 = "https://www.bitmex.com/api/v1/trade?symbol=XBTUSD&startTime=2000-09-28T20:31:13.436&endTime=2000-09-28T20:31:13.437";

    @Autowired
    private RestService restService;

    @Test
    public void shouldSuccessfullyGet() {
        HttpEntity<TradeResponse[]> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<TradeResponse[]> response = restService.get(BITMEX_URL, entity, TradeResponse[].class);
        assertEquals("Status OK", HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldSuccessfullyGet2() {
        HttpEntity<TradeResponse[]> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<TradeResponse[]> response = restService.get(BITMEX_URL2, entity, TradeResponse[].class);
        assertEquals("Status OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Not null body", response.getBody());
        assertEquals("Zero entry", 0, response.getBody().length);
    }
}
