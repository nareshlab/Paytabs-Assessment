package com.example.core;

import com.example.core.dto.TransactionRequest;
import com.example.core.dto.TransactionResponse;
import com.example.core.repo.CardRepository;
import com.example.core.service.ProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProcessingServiceTest {

    @Autowired
    private ProcessingService service;

    @Autowired
    private CardRepository cards;

    private final String cardNumber = "4123456789012345"; // seeded

    @BeforeEach
    void assertSeeded() {
        assertTrue(cards.count() > 0, "Cards should be seeded");
    }

    @Test
    void withdrawInsufficientBalance() {
        TransactionRequest req = new TransactionRequest();
        req.setCardNumber(cardNumber);
        req.setPin("1234");
        req.setAmount(new BigDecimal("9999999"));
        req.setType("withdraw");
        TransactionResponse resp = service.process(req);
        assertFalse(resp.isSuccess());
        assertEquals("Insufficient balance", resp.getMessage());
    }

    @Test
    void topupSuccess() {
        TransactionRequest req = new TransactionRequest();
        req.setCardNumber(cardNumber);
        req.setPin("1234");
        req.setAmount(new BigDecimal("50.00"));
        req.setType("topup");
        TransactionResponse resp = service.process(req);
        assertTrue(resp.isSuccess());
        assertEquals("Approved", resp.getMessage());
        assertNotNull(resp.getBalance());
    }

    @Test
    void invalidPinDeclined() {
        TransactionRequest req = new TransactionRequest();
        req.setCardNumber(cardNumber);
        req.setPin("0000");
        req.setAmount(new BigDecimal("10.00"));
        req.setType("withdraw");
        TransactionResponse resp = service.process(req);
        assertFalse(resp.isSuccess());
        assertEquals("Invalid PIN", resp.getMessage());
    }
}
