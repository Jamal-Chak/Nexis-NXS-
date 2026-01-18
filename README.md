# 🧱 Nexis (NXS)

> **Nexis** is a Layer-1 cryptocurrency blockchain built from scratch in Java.
> **NXS** is the native coin of the Nexis network.

## 🎯 Project Goal
The goal of this project is to build a fully functional blockchain to understand the core principles of cryptocurrency:
- **Cryptography**: SHA-256, ECDSA signatures, Public/Private keys.
- **Consensus**: Proof of Work (PoW) mining.
- **Networking**: P2P decentralized node communication.
- **Storage**: Custom blockchain persistence.

## 🛠 Tech Stack
- **Language**: Java 17+
- **Build Tool**: Maven
- **Cryptography**: `java.security` (Standard Lib) + Bouncy Castle (if needed later)
- **Networking**: `java.net` / `java.nio`

## 🚀 Getting Started

### Prerequisites
- Java JDK 17 or higher
- Maven

### Build
```bash
mvn clean install
```

### Run
*Coming in Phase 9 (CLI)*

## 📂 Project Structure
```
src/main/java/com/nexis/
├── app/        # Entry point
├── core/       # Block, Transaction, Blockchain
├── crypto/     # Hashing, Signatures, Keys
├── consensus/  # Proof of Work logic
├── wallet/     # Wallet management
├── network/    # P2P Networking
├── storage/    # File I/O
└── utils/      # Helpers
```

---
*Built for educational purposes.*
