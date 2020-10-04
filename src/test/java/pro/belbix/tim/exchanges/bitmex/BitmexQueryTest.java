package pro.belbix.tim.exchanges.bitmex;

import org.junit.Test;

import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class BitmexQueryTest {

    //    @Test
    public void buildHttpEntity() {
    }

    //    @Test
    public void buildHeaders() {
    }

    @Test
    public void generateSignature() throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException {
        String sign =
                BitmexQuery.generateSignature(
                        "chNOOS4KvNXR_Xq4k4c9qsfoKWvnDecLATCRlcBwyKDYnWgO",
                        "POST",
                        "https://testnet.bitmex.com/api/v1/order",
                        "1518064238",
                        "{\"symbol\":\"XBTM15\",\"price\":219.0,\"clOrdID\":\"mm_bitmex_1a/oemUeQ4CAJZgP3fjHsA\",\"orderQty\":98}");
        assertEquals("1749cd2ccae4aa49048ae09f0b95110cee706e0944e6a14ad0b3a8cb45bd336b", sign);
    }
}
