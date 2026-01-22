# Nexis Quick Reference

## 🚀 Quick Start Commands

### Build & Run
```bash
# Build the project
mvn clean package

# Run node (interactive)
java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode

# Using management script (Linux/Mac)
./nexis-node.sh start

# Using management script (Windows PowerShell)
.\nexis-node.ps1 start
```

### Management Scripts

**Linux/Mac (`nexis-node.sh`):**
```bash
chmod +x nexis-node.sh          # Make executable
./nexis-node.sh background       # Start in background
./nexis-node.sh status           # Check status
./nexis-node.sh logs             # View logs
./nexis-node.sh stop             # Stop node
./nexis-node.sh restart          # Restart node
```

**Windows (`nexis-node.ps1`):**
```powershell
.\nexis-node.ps1 background      # Start in background
.\nexis-node.ps1 status          # Check status
.\nexis-node.ps1 logs            # View logs
.\nexis-node.ps1 stop            # Stop node
.\nexis-node.ps1 restart         # Restart node
```

---

## 📊 Dashboard & API

### Access Points
```
Dashboard: http://localhost:8000
API Base:  http://localhost:8000/api
```

### API Endpoints
```bash
# Network stats
curl http://localhost:8000/api/stats

# Revenue data
curl http://localhost:8000/api/revenue

# Business metrics
curl http://localhost:8000/api/business

# Full blockchain
curl http://localhost:8000/api/chain

# Mempool
curl http://localhost:8000/api/mempool
```

### With API Key
```bash
curl -H "X-API-KEY: your-key-here" http://localhost:8000/api/stats
```

---

## 💰 CLI Commands

When node is running interactively:

| Command | Description |
|---------|-------------|
| `balance` | Check your NXS balance |
| `send` | Send NXS to an address |
| `mine` | Mine a block (PoW) |
| `stake` | Stake NXS to become validator |
| `pos_mine` | Produce block (PoS) |
| `propose` | Create governance proposal |
| `vote` | Vote on a proposal |
| `deploy` | Deploy smart contract |
| `call` | Call smart contract |
| `chain` | View blockchain |
| `peers` | View connected peers |
| `wallet` | View wallet info |
| `exit` | Exit node |

---

## 🔧 Configuration

### Port Configuration
Default ports:
- Node P2P: 5000
- Dashboard: Port + 3000 (e.g., 8000)

### JVM Memory
```bash
# 4GB heap
java -Xmx4G -Xms2G -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode

# 8GB heap (recommended for production)
java -Xmx8G -Xms4G -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
```

### Private Network Mode
```bash
# Via API (requires admin key)
curl -X POST http://localhost:8000/api/admin/config \
  -H "X-API-KEY: NEXIS_ADMIN_KEY_001"
```

---

## 🌐 Multi-Node Setup

### Node 1 (Bootstrap)
```bash
./nexis-node.sh start
# Enter port: 5000
# Skip peer connection (press Enter)
```

### Node 2 (Connect to Node 1)
```bash
./nexis-node.sh start
# Enter port: 5001
# Enter peer port: 5000
```

### Node 3 (Connect to Network)
```bash
./nexis-node.sh start
# Enter port: 5002
# Enter peer port: 5000  # or 5001
```

---

## 🔐 Security

### Generate API Key
```bash
openssl rand -hex 32
```

### Change Admin Key
Edit code or use environment variable:
```bash
export NEXIS_ADMIN_KEY="your-new-key-here"
```

### Firewall Rules

**Linux (UFW):**
```bash
sudo ufw allow 5000/tcp    # P2P
sudo ufw allow 8000/tcp    # Dashboard (if public)
sudo ufw enable
```

**Windows Firewall:**
```powershell
New-NetFirewallRule -DisplayName "Nexis P2P" -Direction Inbound -LocalPort 5000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Nexis Dashboard" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
```

---

## 💾 Backup & Restore

### Backup
```bash
# Manual backup
cp nexis_chain.json nexis_chain.backup.$(date +%Y%m%d).json
cp revenue_stats.json revenue_stats.backup.$(date +%Y%m%d).json

# Automated backup script
tar -czf nexis-backup-$(date +%Y%m%d).tar.gz nexis_chain.json revenue_stats.json
```

### Restore
```bash
# Stop node first
./nexis-node.sh stop

# Restore files
cp nexis_chain.backup.20260122.json nexis_chain.json
cp revenue_stats.backup.20260122.json revenue_stats.json

# Start node
./nexis-node.sh background
```

---

## 🐛 Troubleshooting

### Node Won't Start
```bash
# Check Java version (must be 17+)
java -version

# Check port availability
netstat -an | grep 5000      # Linux/Mac
netstat -an | findstr 5000   # Windows

# Check logs
cat logs/nexis.log           # Linux/Mac
Get-Content logs\nexis.log   # Windows
```

### Dashboard Not Loading
```bash
# Verify node is running
./nexis-node.sh status

# Test API directly
curl http://localhost:8000/api/stats

# Check firewall
sudo ufw status               # Linux
Get-NetFirewallRule -DisplayName "Nexis*"  # Windows
```

### Peers Not Connecting
- Verify firewall allows port 5000
- Ensure nodes are on same network (not mixed private/public)
- Check peer addresses in configuration
- Verify both nodes are running

### Blockchain Corruption
```bash
# Validate chain (from CLI)
chain

# If invalid, restore from backup or resync
rm nexis_chain.json
./nexis-node.sh restart  # Will sync from peers
```

---

## 📊 Monitoring

### Check Node Status
```bash
# Using management script
./nexis-node.sh status

# Using API
curl http://localhost:8000/api/stats | jq .

# Check process
ps aux | grep nexis           # Linux/Mac
Get-Process | Where-Object {$_.Name -like "*java*"}  # Windows
```

### View Logs
```bash
# Tail logs
./nexis-node.sh logs

# Or manually
tail -f logs/nexis.log        # Linux/Mac
Get-Content logs\nexis.log -Wait  # Windows
```

### Monitor Resources
```bash
# CPU & Memory
top                           # Linux/Mac
htop                          # Linux (if installed)
Task Manager                  # Windows

# Disk usage
df -h                         # Linux/Mac
Get-PSDrive                   # Windows
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Tests
```bash
mvn test -Dtest=CryptoTest
mvn test -Dtest=TestPhase*
mvn test -Dtest=TestHighFees
```

### Manual Testing
```bash
# Start node
./nexis-node.sh start

# Use CLI commands
balance
send
mine
stake
```

---

## 📈 Performance Tuning

### JVM Options
```bash
java -Xmx8G -Xms4G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=./logs/ \
  -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
```

### Storage
- Use SSD for blockchain data
- Disable write caching for data integrity
- Use ext4 (Linux) or NTFS (Windows)

---

## 📚 Resources

- [README](../README.md) - Full documentation
- [Deployment Guide](docs/DEPLOYMENT_GUIDE.md) - Production setup
- [Production Checklist](docs/PRODUCTION_CHECKLIST.md) - Pre-deployment verification
- [Walkthrough](../artifacts/walkthrough.md) - Development changelog

---

## 🆘 Support

- **GitHub Issues**: https://github.com/Jamal-Chak/Nexis-NXS-/issues
- **Documentation**: https://github.com/Jamal-Chak/Nexis-NXS-/tree/main/docs
- **Email**: support@nexis.network

---

**Last Updated**: 2026-01-22
