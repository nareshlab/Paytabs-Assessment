package com.example.core.controller;

import com.example.core.repo.CardRepository;
import com.example.core.repo.TxnRepository;
import com.example.core.util.HashUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class QueryController {

    private final CardRepository cards;
    private final TxnRepository txns;

    public QueryController(CardRepository cards, TxnRepository txns) {
        this.cards = cards;
        this.txns = txns;
    }

    // ✅ Fixed: return plain BigDecimal instead of Spring error JSON
    @GetMapping("/customer/{cardNumber}/balance")
    public ResponseEntity<BigDecimal> balance(@PathVariable("cardNumber") String cardNumber) {
    String hash = HashUtil.sha256(cardNumber);
    return cards.findById(hash)
                .map(c -> ResponseEntity.ok(c.getBalance()))
                .orElseGet(() -> ResponseEntity.ok(BigDecimal.ZERO)); // ✅ use BigDecimal.ZERO
    }

    // ✅ Fixed: return empty list instead of error JSON
    @GetMapping("/customer/{cardNumber}/transactions")
    public ResponseEntity<List<?>> customerTxns(@PathVariable("cardNumber") String cardNumber) {
        String hash = HashUtil.sha256(cardNumber);
        if (cards.existsById(hash)) {
            return ResponseEntity.ok(txns.findByCardNumberOrderByCreatedAtDesc(maskCard(cardNumber)));
        }
        return ResponseEntity.ok(List.of()); // ✅ return empty list instead of error JSON
    }

    // Admin: get all transactions
    @GetMapping("/admin/transactions")
    public ResponseEntity<?> allTxns() {
        // Returns masked card numbers already stored
        return ResponseEntity.ok(txns.findAll());
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return cardNumber.substring(0,4) + "********" + cardNumber.substring(cardNumber.length()-4);
    }
}
