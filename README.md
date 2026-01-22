# 🧱 Nexis (NXS) - Production Blockchain Platform

> **Nexis** is a production-ready Layer-1 cryptocurrency blockchain built from scratch in Java.  
> **NXS** is the native coin of the Nexis network with a capped supply of 21,000,000 NXS.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java 17+](https://img.shields.io/badge/java-17%2B-blue)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()

## 🎯 Project Overview

Nexis is a fully functional blockchain platform designed for both learning and production use. It implements:
- **Cryptography**: SHA-256, ECDSA signatures, Public/Private keys
- **Dual Consensus**: Proof of Work (PoW) AND Proof of Stake (PoS)
- **Fee Economics**: Transaction fees with validator rewards
- **Business Metrics**: Real-time revenue tracking and analytics
- **Networking**: P2P decentralized node communication
- **Governance**: On-chain proposal and voting system
- **Smart Contracts**: Basic VM for contract execution
- **Persistence**: Custom blockchain storage with crash recovery

## 🚀 Quick Start

### Prerequisites
- Java JDK 17 or higher
- Maven 3.6+
- 4GB RAM minimum
- Port 5000+ available

### Build
```bash
mvn clean compile
mvn package
```

### Run a Node
```bash
java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
```

Or use the provided scripts:
```powershell
# Windows
.\run_node.bat

# PowerShell
.\start_node.ps1
```

### Access Dashboard
Once your node is running, access the business dashboard at:
```
http://localhost:8000
```

## 💰 Tokenomics

| Parameter | Value |
|-----------|-------|
| Coin Name | Nexis (NXS) |
| Max Supply | 21,000,000 NXS |
| Block Reward | 50 NXS (halving enabled) |
| Halving Interval | Every 210,000 blocks |
| Treasury Allocation | 10% of all rewards |
| Consensus | PoW + PoS (hybrid) |
| Block Time | ~2 minutes (PoW), ~30 seconds (PoS) |
| Premine | None |

## 📊 Dashboard Features

The Nexis Dashboard provides real-time network analytics:

- **Network Revenue**: Total fees generated, validator earnings
- **Active Wallets**: Unique participants on the network
- **Transaction Metrics**: Average fees, transaction volume
- **Treasury Status**: Protocol treasury balance
- **Validator Economics**: Cost vs reward analysis
- **Network Health**: Peer count, blockchain height, mempool status
- **Live Charts**: Revenue trends, network load visualization

## 🔌 API Endpoints

Nexis exposes a comprehensive REST API for network interaction:

### Network Stats
```bash
GET /api/stats
```
Returns blockchain height, supply, mempool size, peer count

### Revenue Metrics
```bash
GET /api/revenue
```
Returns lifetime and daily fee/reward data

### Business Summary
```bash
GET /api/business
```
Returns active wallets, network load, validator revenue

### Blockchain Data
```bash
GET /api/chain
GET /api/mempool
```

### Rate Limiting
- **Free Tier**: 10 requests/minute
- **Pro Tier**: 100 requests/minute (API key required)
- **Enterprise**: Unlimited (contact for access)

Add API key via header: `X-API-KEY: your-key-here`

## 🏗 Project Structure
```
src/main/java/com/nexis/
├── app/           # Entry point & CLI
├── core/          # Block, Transaction, Blockchain, Fee Model
├── crypto/        # Hashing, Signatures, Keys
├── consensus/     # Proof of Work logic
├── wallet/        # Wallet management
├── network/       # P2P Networking, HTTP API Server
├── storage/       # Blockchain persistence
├── vm/            # Smart contract virtual machine
└── utils/         # Helpers
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run specific test suites:
```bash
# Core cryptography tests
mvn test -Dtest=CryptoTest

# Blockchain validation tests
mvn test -Dtest=TestPhase*

# High fee scenario tests
mvn test -Dtest=TestHighFees
```

## 🔐 Security Features

- ✅ Full transaction signature verification
- ✅ Double-spend prevention
- ✅ Replay attack protection
- ✅ Chain validation on every block
- ✅ API rate limiting
- ✅ Permissioned validator access
- ✅ Input validation on all endpoints

## 💡 Use Cases

### Educational
- Learn blockchain fundamentals
- Understand consensus mechanisms
- Study cryptocurrency economics

### Production
- Private enterprise blockchanes
- Internal accounting ledgers
- Proof-of-concept tokenization
- Distributed state management

### Infrastructure-as-a-Service
- Hosted node services
- Managed validator operations
- API access tiers
- Custom network deployments

## 📈 Business Model

Nexis supports multiple revenue streams:

1. **Managed Nodes**: Monthly hosting subscriptions
2. **API Access**: Tiered pricing for developers
3. **Private Networks**: Enterprise licensing
4. **Validator Services**: Staking-as-a-service
5. **Consulting**: Custom deployment support

See [docs/REVENUE_STREAMS.md](docs/REVENUE_STREAMS.md) for details.

## 📚 Documentation

- [Economic Whitepaper](docs/NEXIS_ECONOMIC_WHITEPAPER.md)
- [Revenue Streams](docs/REVENUE_STREAMS.md)
- [Deployment Guide](docs/DEPLOYMENT_GUIDE.md)

## 🛠 Advanced Features

### Proof of Stake
```java
// Stake NXS to become a validator
blockchain.stake(publicKey, 100.0);

// Produce blocks via PoS
blockchain.mineMempoolPoS(validatorWallet);
```

### Governance
```java
// Create proposal
blockchain.createProposal("prop-001", "Increase block size", myAddress);

// Vote (weight by stake)
blockchain.vote("prop-001", myPublicKey);
```

### Smart Contracts
```java
// Deploy contract
blockchain.deployContract(ownerAddress, "PUSH:10 PUSH:20 ADD");

// Call contract
int result = blockchain.callContract(contractAddress, 5);
```

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure `mvn test` passes
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🌐 Links

- **Repository**: https://github.com/Jamal-Chak/Nexis-NXS-
- **Issues**: Report bugs or request features
- **Discussions**: Join the community

---

**Built with ☕ and ⚡** | Production-ready blockchain from scratch
