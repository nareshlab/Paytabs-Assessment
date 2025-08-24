# Simplified Banking System POC – Detailed Documentation

## 1. Objective
Proof-of-Concept for a simplified banking platform demonstrating:
- Transaction gateway & routing (System 1)
- Core banking processing (System 2)
- Withdraw & Top-up flows
- Card validation, PIN authentication (SHA-256)
- Cryptographic storage of card numbers & PINs
- Role-based monitoring (Customer vs Super Admin)
- In-memory persistence (H2) and traceable test coverage

## 2. High-Level Architecture
```
+-----------+        +------------------+        +-------------------+
|  Browser  | <----> |  System 1        | --->   |  System 2         |
| React UI  |  API   |  Gateway (8081)  |  REST  |  Core (8082)      |
+-----------+        +------------------+        +-------------------+
      |                        |                           |
      | Customer withdraw/topup| Forwards if card starts   | Validates card + PIN hash
      | Login / dashboards     | with '4' (range rule)     | Balance check & mutation
      |                        | Otherwise declines        | Persists Txn in H2
```

## 3. Components
| Layer | Purpose | Key Classes / Files |
|-------|---------|---------------------|
| System 1 (Gateway) | Input validation & routing | `TransactionController`, `TransactionGatewayService` |
| System 2 (Core) | Business logic & persistence | `ProcessingService`, `Card`, `Txn`, repositories |
| Security (Crypto) | Hashing utilities | `HashUtil` |
| Auth (POC) | Simple role simulation | `AuthController` |
| Query APIs | Read models for UI | `QueryController` |
| UI (React) | Role-based dashboards | `pages/*.jsx` |
| Tests | Automated validation | `ProcessingServiceTest`, `TransactionControllerTest` |

## 4. Data Model Summary
| Entity | Storage | Notes |
|--------|---------|-------|
| Card | H2 table `CARD` | `cardHash` (SHA-256 of plain card), `pinHash`, `balance` |
| Txn | H2 table `TXN` | Masked `cardNumber` only (first4 + ******** + last4), amount, status, balanceAfter |

No plain card numbers or PINs are persisted.

## 5. Security & Cryptography
- PINs hashed with SHA-256 (`HashUtil.sha256`) both at seed time and authentication.
- Card numbers hashed (SHA-256) before lookup & persistence (`Card.cardHash`).
- Transactions store only masked card values (not reversible).
- PINs never logged; raw inputs never written to DB.
- Admin auth is hard-coded (POC only) – NOT production ready.

## 6. Functional Flow (Withdraw / Top-up)
1. Customer enters card + PIN + amount in UI.
2. UI calls System 1 `POST /transaction`.
3. System 1 validates: cardNumber, pin, amount>0, type in {withdraw, topup}.
4. System 1 applies routing rule: if card starts with '4' → forward to System 2 `/process`; else decline (`Card range not supported`).
5. System 2 hashes card number → lookup Card; if missing → decline.
6. Hash entered PIN → compare to stored pinHash; mismatch → decline.
7. Apply business rule:
   - withdraw: ensure balance >= amount.
   - topup: add amount.
8. Persist Txn (SUCCESS or DECLINED + reason) with masked card.
9. Return response (success flag, message, new balance when success).
10. UI refreshes balance & transaction list.

## 7. Requirements Traceability Matrix
| Req | Description | Implementation Reference |
|-----|-------------|--------------------------|
| 1 | Route only cards starting with '4' | `TransactionGatewayService.route()` |
| 2 | Validate required fields / positive amount | `TransactionRequest` (Jakarta validation) both systems |
| 3 | Types limited to withdraw/topup | Controllers enforce + service switch |
| 4 | Decline unsupported range | `TransactionGatewayService` returns message |
| 5 | Card existence check | `ProcessingService.process()` (hash lookup) |
| 6 | PIN hash verification | `ProcessingService` + `HashUtil` |
| 7 | Balance check (withdraw) | `ProcessingService` branch for withdraw |
| 8 | Top-up applies credit | `ProcessingService` branch for topup |
| 9 | Persist masked transactions | `Txn.cardNumber` + `maskCard()` |
| 10 | PIN hashing (SHA-256) | `HashUtil` + seeding in `CoreApplication` |
| 11 | Card hashing storage | `Card.cardHash` + seeding |
| 12 | Role-based monitoring | React `AdminDashboard` vs `CustomerDashboard` |
| 13 | Customer transaction visibility | `GET /customer/{card}/transactions` |
| 14 | Customer balance query | `GET /customer/{card}/balance` |
| 15 | Admin all transactions | `GET /admin/transactions` |
| 16 | Tests cover key scenarios | JUnit tests in both modules |
| 17 | In-memory DB | H2 config in `application.properties` |

## 8. API Specification
### System 1 (Gateway 8081)
POST /transaction
Request:
```
{
  "cardNumber": "4123456789012345",
  "pin": "1234",
  "amount": 100.00,
  "type": "withdraw" | "topup"
}
```
Responses (200 always in current design):
- Success: `{ "success": true, "message": "Approved", "transactionId": "...", "balance": 4940.00 }`
- Decline examples: `{"success": false, "message": "Card range not supported"}`

### System 2 (Core 8082)
| Method & Path | Purpose | Notes |
|---------------|---------|-------|
| POST /process | Core processing | Same body as gateway request |
| POST /auth/login | Customer login | Returns `{ok, role, cardNumber}` |
| POST /auth/adminLogin | Admin login | POC only (`admin/admin`) |
| GET /customer/{card}/balance | Balance number | Returns numeric JSON (e.g., 5000.00) |
| GET /customer/{card}/transactions | Customer tx list | Masked card stored |
| GET /admin/transactions | All transactions | Admin dashboard feed |

### Transaction Status Meanings
- SUCCESS: Funds moved & balance updated.
- DECLINED: No balance change; reason populated.

## 9. Setup & Run
### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 18+

### Start Core (System 2)
PowerShell:
```
cd system2-core
mvn spring-boot:run
```
Seeds cards:
- 4123456789012345 / PIN 1234 / 5000.00
- 4987654321098765 / PIN 4321 / 3000.00

### Start Gateway (System 1)
```
cd system1-gateway
mvn spring-boot:run
```

### Start UI
```
cd ui-react
npm install
npm run dev
```
Visit http://localhost:5173

## 10. Manual Test Script (cURL)
(Use PowerShell; install `jq` optionally.)

1. Successful top-up:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"4123456789012345","pin":"1234","amount":100,"type":"topup"}'
```
2. Successful withdrawal:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"4123456789012345","pin":"1234","amount":50,"type":"withdraw"}'
```
3. Unsupported range:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"5123456789012345","pin":"1234","amount":10,"type":"topup"}'
```
4. Invalid card:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"4000000000000000","pin":"1234","amount":10,"type":"withdraw"}'
```
5. Invalid PIN:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"4123456789012345","pin":"9999","amount":10,"type":"withdraw"}'
```
6. Insufficient balance:
```
curl -Method POST -Uri http://localhost:8081/transaction -Headers @{"Content-Type"="application/json"} -Body '{"cardNumber":"4123456789012345","pin":"1234","amount":999999,"type":"withdraw"}'
```
7. Balance query:
```
curl http://localhost:8082/customer/4123456789012345/balance
```
8. Admin all txns:
```
curl http://localhost:8082/admin/transactions
```

## 11. Automated Tests
| Module | Test | Scenario |
|--------|------|----------|
| Gateway | `TransactionControllerTest.rejectsNonSupportedCardRange` | Card range decline |
| Core | `ProcessingServiceTest.topupSuccess` | Top-up approval |
| Core | `ProcessingServiceTest.withdrawInsufficientBalance` | Insufficient funds decline |
| Core | `ProcessingServiceTest.invalidPinDeclined` | Wrong PIN decline |

Run all tests:
```
mvn -q test
```

## 12. Success Criteria (Met)
| Criterion | Status |
|-----------|--------|
| Routing based on leading '4' | Implemented (Gateway) |
| Card + PIN validation & hashing | Implemented (Core) |
| Withdraw/top-up balance logic | Implemented |
| Super Admin sees all txns | UI + `/admin/transactions` |
| Customer sees own balance & txns | UI + customer endpoints |
| Decline cases handled | All enumerated |
| In-memory DB | H2 configured |
| No plain PIN/card storage | Confirmed (hash & mask) |
| Tests for key flows | Added |



## 13. Glossary
| Term | Meaning |
|------|---------|
| Top-up | Increase card balance |
| Withdraw | Decrease card balance if sufficient funds |
| Masked Card | First 4 + ******** + last 4 digits |
| PIN Hash | SHA-256 digest of plain PIN |

---
End of document.
