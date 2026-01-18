# 🧱 Nexis (NXS) — Java Cryptocurrency Project TODO

> Project: **Nexis**
> Coin: **NXS**
> Type: Layer-1 Blockchain (Built from scratch in Java)
> Goal: Learn, design, and implement a real cryptocurrency end-to-end

---

## 📌 PHASE 0 — Project Setup & Foundations

### Repo & Tooling
- [x] Create Git repository: `nexis-core`
- [x] Initialize Java project (Java 17+)
- [x] Choose build tool (Maven or Gradle)
- [x] Add `.gitignore`
- [x] Add `README.md` describing Nexis and NXS

### Base Package
- [x] Use base package: `com.nexis`
- [x] Create core folder structure:

src/main/java/com/nexis/
├── app/
├── crypto/
├── core/
├── consensus/
├── wallet/
├── network/
├── storage/
└── utils/

---

## 🔐 PHASE 1 — Cryptography (Identity & Trust)

> Everything in Nexis depends on cryptography. No shortcuts.

### Tasks
- [x] Implement SHA-256 hashing utility
- [x] Generate ECDSA public/private key pairs
- [x] Encode public keys as Nexis addresses
- [x] Sign data using private keys
- [x] Verify signatures using public keys

### Files

crypto/
├── HashUtil.java
├── KeyPairUtil.java
└── SignatureUtil.java

### Learning Goal
- Understand identity without usernames
- Understand why signatures = ownership

---

## 💸 PHASE 2 — Transactions (Value Transfer)

> Transactions move **NXS** between addresses.

### Tasks
- [x] Create `Transaction` class
- [x] Add sender address
- [x] Add recipient address
- [x] Add amount (NXS)
- [x] Add timestamp
- [x] Sign transactions
- [x] Verify transaction signatures
- [x] Reject invalid or forged transactions

### Files

core/
└── Transaction.java

wallet/
└── Wallet.java

### Learning Goal
- Learn how trustless payments work
- Understand double-spending risks

---

## 📦 PHASE 3 — Blocks (Immutability)

> Blocks group transactions and lock them forever.

### Tasks
- [x] Create `Block` class
- [x] Add index, timestamp, transactions
- [x] Add previous block hash
- [x] Calculate block hash
- [x] Validate block integrity

### Files

core/
└── Block.java

### Learning Goal
- Understand immutability
- Learn how tampering is detected

---

## ⛓ PHASE 4 — Blockchain Ledger (Single Node)

> Start with one node. Networking comes later.

### Tasks
- [x] Create `Blockchain` class
- [x] Define Nexis genesis block
- [x] Maintain ordered chain of blocks
- [x] Validate full chain
- [x] Track balances in NXS
- [x] Prevent double-spending
- [x] Maintain transaction mempool

### Files

core/
└── Blockchain.java

### Learning Goal
- Understand ledgers vs databases
- Learn why rules matter more than code

---

## ⚙️ PHASE 5 — Consensus (Proof of Work)

> Nexis initially uses Proof of Work.

### Tasks
- [x] Define mining difficulty
- [x] Implement nonce-based mining
- [x] Validate block difficulty
- [x] Create coinbase transaction
- [x] Reward miners with NXS
- [x] Enforce max supply (21,000,000 NXS)

### Files

consensus/
└── ProofOfWork.java

### Learning Goal
- Understand cost-based security
- Learn why mining resists attacks

---

## 🪙 Nexis Monetary Policy (LOCKED RULES)

- Coin Name: **Nexis**
- Symbol: **NXS**
- Max Supply: **21,000,000 NXS**
- Block Reward: **50 NXS**
- Halving Interval: **210,000 blocks**
- Premine: ❌ None
- Admin Minting: ❌ Disabled

---

## 💾 PHASE 6 — Persistence & Storage

> Nexis must survive restarts.

### Tasks
- [x] Persist blockchain to disk
- [x] Reload blockchain on startup
- [x] Persist mempool transactions
- [x] Handle corrupted data safely

### Files

storage/
└── ChainStore.java

### Learning Goal
- Understand durability
- Learn node reliability principles

---

## 🌐 PHASE 7 — Networking (Multi-Node Nexis)

> Decentralization begins here.

### Tasks
- [x] Create Nexis node identity
- [x] Implement peer discovery
- [x] Sync blockchain between nodes
- [x] Broadcast NXS transactions
- [x] Broadcast mined blocks
- [x] Resolve forks (longest valid chain)

### Files

network/
├── Node.java
├── PeerManager.java
└── MessageHandler.java

### Learning Goal
- Learn distributed systems fundamentals
- Understand real decentralization

---

## 🧪 PHASE 8 — Security & Validation

> Assume attackers exist.

### Tasks
- [x] Validate all inbound data
- [x] Reject malformed blocks
- [x] Enforce transaction limits
- [x] Protect against replay attacks
- [x] Add logging & monitoring

---

## 🧭 PHASE 9 — CLI / Node App

> Make Nexis usable.

### Tasks
- [x] Start Nexis node from CLI
- [x] Create wallet
- [x] Send NXS
- [x] Check balance
- [x] Mine blocks manually or automatically

### Files

app/
└── NexisNode.java

---

## 🚀 PHASE 10 — Future Evolution (Optional)

- [x] Proof of Stake
- [x] Transaction fees
- [x] Governance
- [x] Smart contract VM
- [x] TwineEngine ecosystem integration

---

## 🧠 PROJECT RULES

- ❌ No copy-paste without understanding
- ✅ Commit frequently
- ✅ Break things intentionally to learn
- ✅ Document decisions
- ❌ No features before fundamentals

---

## 🏁 FINAL GOAL

> Build **Nexis (NXS)** as a real, working blockchain
> and understand every line of code that secures it.

https://github.com/Jamal-Chak/Nexis-NXS-.git