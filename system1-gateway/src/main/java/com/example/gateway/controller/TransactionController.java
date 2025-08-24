package com.example.gateway.controller;

import com.example.gateway.dto.TransactionRequest;
import com.example.gateway.dto.TransactionResponse;
import com.example.gateway.service.TransactionGatewayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionGatewayService service;

    public TransactionController(TransactionGatewayService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> handle(@Valid @RequestBody TransactionRequest request) {
        // Validate type
        String t = request.getType() == null ? "" : request.getType().toLowerCase();
        if (!t.equals("withdraw") && !t.equals("topup")) {
            return ResponseEntity.badRequest().body(new TransactionResponse(false, "Invalid type (use 'withdraw' or 'topup')"));
        }
        TransactionResponse resp = service.route(request);
        return ResponseEntity.ok(resp);
    }
}
