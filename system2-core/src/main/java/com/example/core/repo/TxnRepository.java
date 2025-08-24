package com.example.core.repo;

import com.example.core.model.Txn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TxnRepository extends JpaRepository<Txn, String> {
    // cardNumber stored masked (first4 + ******** + last4)
    List<Txn> findByCardNumberOrderByCreatedAtDesc(String cardNumber);
}
