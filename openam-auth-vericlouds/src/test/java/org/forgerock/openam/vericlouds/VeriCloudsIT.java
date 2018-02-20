package org.forgerock.openam.vericlouds;

import org.springframework.http.*;
import java.net.Proxy;
import java.net.InetSocketAddress;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VeriCloudsIT {

    public static final String AM_AUTHENTICATE_ENDPOINT = "http://openam.partner.com:8080/openam/json/authenticate";

    // @Test
    // public void testGetToken() throws Exception {
    //     // SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();
    //     // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8123));
    //     // clientHttpReq.setProxy(proxy);
    //     // RestTemplate restTemplate = new RestTemplate(clientHttpReq);

    //     RestTemplate restTemplate = new RestTemplate();
    //     HttpHeaders httpHeaders = new HttpHeaders();
    //     httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    //     //Get Initial Auth ID
    //     ResponseEntity<SampleAuthCallback> entity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
    //             HttpMethod.POST, new HttpEntity<>(httpHeaders), SampleAuthCallback.class);
    //     SampleAuthCallback callback = entity.getBody();

    //     //Set correct username and password
    //     callback.setUsernameAndPassword("amadmin", "password");

    //     //Authenticate to OpenAM
    //     ResponseEntity<String> tokenEntity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
    //             HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);

    //     //Assert response is 200 and print token
    //     Assert.assertEquals(tokenEntity.getStatusCode(), HttpStatus.OK);
    //     System.out.println(tokenEntity.getBody());
    // }

    @Test
    public void testFailGetToken1() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Get Initial Auth ID
        ResponseEntity<SampleAuthCallback> entity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                HttpMethod.POST, new HttpEntity<>(httpHeaders), SampleAuthCallback.class);
        SampleAuthCallback callback = entity.getBody();

        //Set incorrect username and password
        callback.setUsernameAndPassword("amadmin", "password");

        //Authenticate to OpenAM
        try {
            restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                    HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);
        } catch (HttpClientErrorException e) {
            //Assert response is 401
            Assert.assertEquals(e.getStatusCode(), HttpStatus.UNAUTHORIZED);
            return;

        }
        // Fail if 401 isn't received
        Assert.fail();
    }

    @Test
    public void testFailGetToken2() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Get Initial Auth ID
        ResponseEntity<SampleAuthCallback> entity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                HttpMethod.POST, new HttpEntity<>(httpHeaders), SampleAuthCallback.class);
        SampleAuthCallback callback = entity.getBody();

        //Set incorrect username and password
        callback.setUsernameAndPassword("demo", "changeit");

        //Authenticate to OpenAM
        try {
            restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                    HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);
        } catch (HttpClientErrorException e) {
            //Assert response is 401
            Assert.assertEquals(e.getStatusCode(), HttpStatus.UNAUTHORIZED);
            return;

        }
        // Fail if 401 isn't received
        Assert.fail();
    }
}
