package com.zhuinden.rxrealm;

import com.zhuinden.rxrealm.path.cat.CatsBO;

import org.junit.Test;
import org.simpleframework.xml.core.Persister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private static final String CAT_API_RESPONSE = "<response>\n" +
            "<data>\n" +
            "<images>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://29.media.tumblr.com/zkfIIODV5g3waek7dZlwg4bMo1_500.jpg\n" +
            "</url>\n" +
            "<id>2dg</id>\n" +
            "<source_url>http://thecatapi.com/?id=2dg</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://24.media.tumblr.com/gZxdcfE6hihx5ladSMJ2Ij0Oo1_500.jpg\n" +
            "</url>\n" +
            "<id>4fh</id>\n" +
            "<source_url>http://thecatapi.com/?id=4fh</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://24.media.tumblr.com/tumblr_m3zba1LIZx1qi23vmo1_500.jpg\n" +
            "</url>\n" +
            "<id>3v9</id>\n" +
            "<source_url>http://thecatapi.com/?id=3v9</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_m60ilb2dXz1qz5dg8o1_1280.jpg\n" +
            "</url>\n" +
            "<id>MTg0MjY0Mw</id>\n" +
            "<source_url>http://thecatapi.com/?id=MTg0MjY0Mw</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://29.media.tumblr.com/tumblr_m33r8snztd1rtuomto1_500.jpg\n" +
            "</url>\n" +
            "<id>2g6</id>\n" +
            "<source_url>http://thecatapi.com/?id=2g6</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_m1i9hkZBow1qzex9io1_1280.jpg\n" +
            "</url>\n" +
            "<id>5uu</id>\n" +
            "<source_url>http://thecatapi.com/?id=5uu</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://30.media.tumblr.com/tumblr_lwibfeamvs1qbhms5o1_1280.jpg\n" +
            "</url>\n" +
            "<id>1oc</id>\n" +
            "<source_url>http://thecatapi.com/?id=1oc</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_lteldggkEL1r4xjo2o1_1280.jpg\n" +
            "</url>\n" +
            "<id>5f6</id>\n" +
            "<source_url>http://thecatapi.com/?id=5f6</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://24.media.tumblr.com/tumblr_lqh6lhKm9v1qz5dg8o1_1280.jpg\n" +
            "</url>\n" +
            "<id>ccv</id>\n" +
            "<source_url>http://thecatapi.com/?id=ccv</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_lzpbnwVXLp1rodibgo1_1280.jpg\n" +
            "</url>\n" +
            "<id>ct3</id>\n" +
            "<source_url>http://thecatapi.com/?id=ct3</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://28.media.tumblr.com/tumblr_lyb78mCojl1r189uao1_500.jpg\n" +
            "</url>\n" +
            "<id>j9</id>\n" +
            "<source_url>http://thecatapi.com/?id=j9</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_lvnu1qoRgO1qbd47zo1_500.jpg\n" +
            "</url>\n" +
            "<id>d2b</id>\n" +
            "<source_url>http://thecatapi.com/?id=d2b</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_m4habbbLPa1qd477zo1_1280.jpg\n" +
            "</url>\n" +
            "<id>bu0</id>\n" +
            "<source_url>http://thecatapi.com/?id=bu0</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://24.media.tumblr.com/tumblr_lleefmPnH71qhwmnpo1_1280.jpg\n" +
            "</url>\n" +
            "<id>8q2</id>\n" +
            "<source_url>http://thecatapi.com/?id=8q2</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_m4okzvLLic1qd477zo1_1280.jpg\n" +
            "</url>\n" +
            "<id>dnf</id>\n" +
            "<source_url>http://thecatapi.com/?id=dnf</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_m3lbttXJkE1qze0hyo1_500.jpg\n" +
            "</url>\n" +
            "<id>a49</id>\n" +
            "<source_url>http://thecatapi.com/?id=a49</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/Jjkybd3nSdfnde8gUIqca26Z_500.jpg\n" +
            "</url>\n" +
            "<id>72e</id>\n" +
            "<source_url>http://thecatapi.com/?id=72e</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_lyzl8dbxRw1r6b7kmo1_500.jpg\n" +
            "</url>\n" +
            "<id>7bi</id>\n" +
            "<source_url>http://thecatapi.com/?id=7bi</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://28.media.tumblr.com/tumblr_m378jgiBLn1rula82o1_1280.jpg\n" +
            "</url>\n" +
            "<id>32k</id>\n" +
            "<source_url>http://thecatapi.com/?id=32k</source_url>\n" +
            "</image>\n" +
            "<image>\n" +
            "<url>\n" +
            "http://25.media.tumblr.com/tumblr_lhh15vhFw21qgnva2o1_500.jpg\n" +
            "</url>\n" +
            "<id>bc6</id>\n" +
            "<source_url>http://thecatapi.com/?id=bc6</source_url>\n" +
            "</image>\n" +
            "</images>\n" +
            "</data>\n" +
            "</response>";

    @Test
    public void testCatApiResponseParsedCorrectly()
            throws Exception {
        Persister persister = new Persister();
        try {
            CatsBO cats = persister.read(CatsBO.class, CAT_API_RESPONSE);
            assertEquals("http://29.media.tumblr.com/zkfIIODV5g3waek7dZlwg4bMo1_500.jpg", cats.getCats().get(0).getUrl().trim());
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
}