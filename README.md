# Simplified Banking System POC

This repository contains:
- **System 1 (Gateway)**: Routes transactions to System 2 if card starts with `4`. (`/transaction`)
- **System 2 (Core Banking)**: Validates card, verifies PIN (SHA-256), processes **withdraw/top-up**, stores transactions in H2, and exposes query/auth APIs.
- **React UI**: Role-based UI for **Customer** and **Super Admin**.

## Tech
- Java 17, Spring Boot 3, H2 in-memory DB
- React + Vite

## Ports
- System 1 (Gateway): **8081**
- System 2 (Core): **8082**
- UI: **5173**

---

## 1) Build & Run

### Prereqs
- Java 17+
- Maven 3.9+
- Node 18+ (for UI)

### System 2 (Core)
```bash
cd system2-core
mvn spring-boot:run
```
It seeds two cards:
- `4123456789012345` (PIN: `1234`, balance: 5000.00)
- `4987654321098765` (PIN: `4321`, balance: 3000.00)

H2 console: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:bankdb`)

### System 1 (Gateway)
Open a new terminal:
```bash
cd system1-gateway
mvn spring-boot:run
```

### UI
Open a new terminal:
```bash
cd ui-react
npm install
npm run dev
```
Visit: http://localhost:5173

---

## 2) API Endpoints

### System 1 (Gateway)
- `POST /transaction`
```json
{
  "cardNumber": "4123456789012345",
  "pin": "1234",
  "amount": 100.00,
  "type": "topup" // or "withdraw"
}
```

**Routing rule**: Only routes card numbers starting with `4`. Others => `"Card range not supported"`.

### System 2 (Core Banking)
- `POST /process` — core processing
- `POST /auth/login` — customer login via card+pin
- `POST /auth/adminLogin` — admin login (admin/admin) [POC only]
- `GET /customer/{card}/balance` — current balance
- `GET /customer/{card}/transactions` — transactions for card
- `GET /admin/transactions` — all transactions

---

## 3) cURL Examples

### ✅ Successful Top-up
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"4123456789012345","pin":"1234","amount":100,"type":"topup"}' | jq .
```

### ✅ Successful Withdrawal
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"4123456789012345","pin":"1234","amount":50,"type":"withdraw"}' | jq .
```

### ❌ Unsupported Card Range
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"5123456789012345","pin":"1234","amount":10,"type":"topup"}'
```

### ❌ Invalid Card
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"4000000000000000","pin":"1234","amount":10,"type":"withdraw"}'
```

### ❌ Invalid PIN
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"4123456789012345","pin":"9999","amount":10,"type":"withdraw"}'
```

### ❌ Insufficient Balance
```bash
curl -s -X POST http://localhost:8081/transaction   -H "Content-Type: application/json"   -d '{"cardNumber":"4123456789012345","pin":"1234","amount":999999,"type":"withdraw"}'
```

### Query Balance (System 2)
```bash
curl -s http://localhost:8082/customer/4123456789012345/balance
```

### Admin - All Transactions
```bash
curl -s http://localhost:8082/admin/transactions | jq '.[0:5]'
```

---

## 4) How the Flow Works

1. **UI (Customer)** logs in via `POST /auth/login` (card+pin checked using **SHA-256** hash).
2. Customer triggers a **Top-up/Withdrawal** → UI calls **System 1** `POST /transaction`.
3. System 1 validates inputs and **routes** only if card starts with `4` → forwards to **System 2** `/process`.
4. System 2 validates card, **hashes PIN** and compares, checks balance, updates **H2** DB, and saves a **Txn** record.
5. Admin UI calls `GET /admin/transactions` to see **all** transactions.

> Security note: For POC, customer/admin sessions are not tokenized. Never log/store **plain-text PINs**; only hashes are stored/compared.

---

## 5) Test Cases Checklist

- ✅ Successful withdrawal/top-up with valid card/PIN.
- ✅ Decline invalid **card** or **PIN**.
- ✅ Decline **insufficient balance** (withdrawal).
- ✅ Decline **unsupported card range** (System 1).
- ✅ Super Admin UI shows **all transactions**.
- ✅ Customer UI shows **own transactions**, **balance**, and supports **top-ups**.

---

## 6) Notes
- Change seed PINs/balances in `CoreApplication#seed` if needed.
- SHA-256 hashing is in `HashUtil`. No plain-text PINs are stored.
