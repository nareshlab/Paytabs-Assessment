package com.example.core;

import com.example.core.model.Card;
import com.example.core.repo.CardRepository;
import com.example.core.util.HashUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class CoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(CardRepository cards) {
        return args -> {
            if (cards.count() == 0) {
                seedCard(cards, "4123456789012345", "1234", "5000.00");
                seedCard(cards, "4987654321098765", "4321", "3000.00");
            }
        };
    }

    private void seedCard(CardRepository repo, String plainCard, String pin, String balance) {
        Card c = new Card();
        c.setCardHash(HashUtil.sha256(plainCard));
        c.setPinHash(HashUtil.sha256(pin));
        c.setBalance(new BigDecimal(balance));
        repo.save(c);
    }
}
