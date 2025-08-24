package com.example.gateway.service;

import com.example.gateway.dto.TransactionRequest;
import com.example.gateway.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionGatewayService {

    @Value("${core.base-url}")
    private String coreBaseUrl;

    private final RestTemplate restTemplate;

    public TransactionGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TransactionResponse route(TransactionRequest req) {
        // Card range rule: only route if it starts with '4'
        if (req.getCardNumber() == null || !req.getCardNumber().startsWith("4")) {
            return new TransactionResponse(false, "Card range not supported");
        }
        // Forward to System 2 /process
        String url = coreBaseUrl + "/process";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransactionRequest> entity = new HttpEntity<>(req, headers);
        try {
            TransactionResponse response = restTemplate.postForObject(url, entity, TransactionResponse.class);
            return response;
        } catch (Exception e) {
            TransactionResponse tr = new TransactionResponse(false, "System 2 unreachable: " + e.getMessage());
            return tr;
        }
    }
}
