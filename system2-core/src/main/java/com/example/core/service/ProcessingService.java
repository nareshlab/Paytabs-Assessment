package com.example.core.service;

import com.example.core.dto.TransactionRequest;
import com.example.core.dto.TransactionResponse;
import com.example.core.model.*;
import com.example.core.repo.CardRepository;
import com.example.core.repo.TxnRepository;
import com.example.core.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProcessingService {

    private final CardRepository cards;
    private final TxnRepository txns;

    public ProcessingService(CardRepository cards, TxnRepository txns) {
        this.cards = cards;
        this.txns = txns;
    }

    @Transactional
    public TransactionResponse process(TransactionRequest req) {
        // NEVER log the PIN; do not print it.
        String type = req.getType().toLowerCase(Locale.ROOT);
    // Hash the incoming card number for lookup (we never store plain numbers)
    String cardHash = HashUtil.sha256(req.getCardNumber());
    Optional<Card> cardOpt = cards.findById(cardHash);
        if (cardOpt.isEmpty()) {
            Txn t = new Txn();
            t.setCardNumber(maskCard(req.getCardNumber()));
            t.setAmount(req.getAmount());
            t.setType("withdraw".equals(type) ? TxnType.WITHDRAW : TxnType.TOPUP);
            t.setStatus(TxnStatus.DECLINED);
            t.setDeclineReason("Invalid card");
            txns.save(t);
            return new TransactionResponse(false, "Invalid card");
        }
        Card card = cardOpt.get();
        String inputHash = HashUtil.sha256(req.getPin());
        if (!inputHash.equals(card.getPinHash())) {
            Txn t = new Txn();
            t.setCardNumber(maskCard(req.getCardNumber()));
            t.setAmount(req.getAmount());
            t.setType("withdraw".equals(type) ? TxnType.WITHDRAW : TxnType.TOPUP);
            t.setStatus(TxnStatus.DECLINED);
            t.setDeclineReason("Invalid PIN");
            txns.save(t);
            return new TransactionResponse(false, "Invalid PIN");
        }

        BigDecimal newBalance;
        Txn t = new Txn();
    t.setCardNumber(maskCard(req.getCardNumber()));
        t.setAmount(req.getAmount());
        if ("withdraw".equals(type)) {
            t.setType(TxnType.WITHDRAW);
            if (card.getBalance().compareTo(req.getAmount()) < 0) {
                t.setStatus(TxnStatus.DECLINED);
                t.setDeclineReason("Insufficient balance");
                txns.save(t);
                return new TransactionResponse(false, "Insufficient balance");
            }
            newBalance = card.getBalance().subtract(req.getAmount());
        } else if ("topup".equals(type)) {
            t.setType(TxnType.TOPUP);
            newBalance = card.getBalance().add(req.getAmount());
        } else {
            t.setStatus(TxnStatus.DECLINED);
            t.setDeclineReason("Invalid type");
            txns.save(t);
            return new TransactionResponse(false, "Invalid type");
        }

        card.setBalance(newBalance);
        cards.save(card);

        t.setStatus(TxnStatus.SUCCESS);
        t.setBalanceAfter(newBalance);
        txns.save(t);

        TransactionResponse resp = new TransactionResponse(true, "Approved");
        resp.setTransactionId(t.getId());
        resp.setBalance(newBalance);
        return resp;
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "****";
        return cardNumber.substring(0,4) + "********" + cardNumber.substring(cardNumber.length()-4);
    }
}
