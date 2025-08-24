package com.example.core.controller;

import com.example.core.dto.TransactionRequest;
import com.example.core.dto.TransactionResponse;
import com.example.core.service.ProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProcessController {

    private final ProcessingService service;

    public ProcessController(ProcessingService service) {
        this.service = service;
    }

    @PostMapping("/process")
    @CrossOrigin(origins = "*")
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody TransactionRequest req) {
        // Validate type in controller to match requirement strictly
        String t = req.getType() == null ? "" : req.getType().toLowerCase();
        if (!t.equals("withdraw") && !t.equals("topup")) {
            return ResponseEntity.badRequest().body(new TransactionResponse(false, "Invalid type (use 'withdraw' or 'topup')"));
        }
        return ResponseEntity.ok(service.process(req));
    }
}
