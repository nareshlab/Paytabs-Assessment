package com.example.gateway;

import com.example.gateway.dto.TransactionRequest;
import com.example.gateway.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void rejectsNonSupportedCardRange() {
        TransactionRequest req = new TransactionRequest();
        req.setCardNumber("5123456789012345");
        req.setPin("0000");
        req.setAmount(new BigDecimal("10.00"));
        req.setType("withdraw");

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<TransactionResponse> resp = rest.postForEntity("/transaction", new HttpEntity<>(req,h), TransactionResponse.class);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isSuccess());
        assertEquals("Card range not supported", resp.getBody().getMessage());
    }
}
