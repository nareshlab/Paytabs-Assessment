package com.example.core.controller;

import com.example.core.dto.AdminLoginRequest;
import com.example.core.dto.LoginRequest;
import com.example.core.repo.CardRepository; // used for hashed card lookups
import com.example.core.util.HashUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final CardRepository cards;

    public AuthController(CardRepository cards) {
        this.cards = cards;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    String cardHash = HashUtil.sha256(req.getCardNumber());
    return cards.findById(cardHash)
                .map(card -> {
                    if (HashUtil.sha256(req.getPin()).equals(card.getPinHash())) {
                        Map<String, Object> resp = new HashMap<>();
            resp.put("ok", true);
            // Return plain card number back (client supplied it) not stored version
            resp.put("cardNumber", req.getCardNumber());
                        resp.put("role", "CUSTOMER");
                        return ResponseEntity.ok(resp);
                    } else {
                        return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Invalid PIN"));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "message", "Invalid card")));
    }

    @PostMapping("/adminLogin")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        // Very simple admin auth for POC (DO NOT use in production)
        if ("admin".equals(req.getUsername()) && "admin".equals(req.getPassword())) {
            return ResponseEntity.ok(Map.of("ok", true, "role", "ADMIN"));
        }
        return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Invalid admin credentials"));
    }
}
