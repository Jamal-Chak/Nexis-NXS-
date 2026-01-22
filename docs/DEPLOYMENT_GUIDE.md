# Nexis Deployment Guide

## Overview

This guide covers deploying Nexis blockchain nodes in various environments, from local development to production infrastructure.

## System Requirements

### Minimum Specifications
- **CPU**: 2 cores, 2.0 GHz
- **RAM**: 4 GB
- **Storage**: 20 GB SSD
- **Network**: 10 Mbps, open ports 5000-5100
- **OS**: Windows 10+, Linux (Ubuntu 20.04+), macOS 11+

### Recommended (Production)
- **CPU**: 4+ cores, 3.0 GHz
- **RAM**: 8 GB
- **Storage**: 100 GB NVMe SSD
- **Network**: 100 Mbps, static IP, firewall configured
- **OS**: Ubuntu 22.04 LTS or Windows Server 2022

## Quick Deployment

### Local Development

1. **Clone Repository**
   ```bash
   git clone https://github.com/Jamal-Chak/Nexis-NXS-.git
   cd Nexis-NXS-
   ```

2. **Build Project**
   ```bash
   mvn clean package
   ```

3. **Run Node**
   ```bash
   java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
   ```

4. **Access Dashboard**
   - Navigate to `http://localhost:8000`

### Multi-Node Local Network

**Terminal 1: Node 1**
```bash
java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
# Enter port: 5000
# Skip peer connection (press Enter)
```

**Terminal 2: Node 2**
```bash
java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
# Enter port: 5001
# Enter peer port: 5000
```

**Terminal 3: Node 3**
```bash
java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
# Enter port: 5002
# Enter peer port: 5000
```

## Production Deployment

### Ubuntu Server

1. **Install Java 17**
   ```bash
   sudo apt update
   sudo apt install openjdk-17-jdk maven git -y
   java -version
   ```

2. **Create Service User**
   ```bash
   sudo useradd -m -s /bin/bash nexis
   sudo su - nexis
   ```

3. **Deploy Application**
   ```bash
   cd ~
   git clone https://github.com/Jamal-Chak/Nexis-NXS-.git
   cd Nexis-NXS-
   mvn clean package -DskipTests
   ```

4. **Create Systemd Service**
   ```bash
   sudo nano /etc/systemd/system/nexis-node.service
   ```

   ```ini
   [Unit]
   Description=Nexis Blockchain Node
   After=network.target

   [Service]
   Type=simple
User=nexis
   WorkingDirectory=/home/nexis/Nexis-NXS-
   ExecStart=/usr/bin/java -Xmx4G -Xms2G -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
   Restart=on-failure
   RestartSec=10
   StandardOutput=journal
   StandardError=journal

   [Install]
   WantedBy=multi-user.target
   ```

5. **Enable and Start Service**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable nexis-node
   sudo systemctl start nexis-node
   sudo systemctl status nexis-node
   ```

6. **View Logs**
   ```bash
   sudo journalctl -u nexis-node -f
   ```

### Windows Server

1. **Install Java 17**
   - Download from [Adoptium](https://adoptium.net/)
   - Add to PATH

2. **Deploy Application**
   ```powershell
   cd C:\Services
   git clone https://github.com/Jamal-Chak/Nexis-NXS-.git
   cd Nexis-NXS-
   mvn clean package -DskipTests
   ```

3. **Create Windows Service**
   - Use [NSSM](https://nssm.cc/) (Non-Sucking Service Manager)
   ```powershell
   nssm install NexisNode "C:\Program Files\Java\jdk-17\bin\java.exe" `
     "-Xmx4G -Xms2G -cp C:\Services\Nexis-NXS-\target\nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode"
   nssm start NexisNode
   ```

## Network Configuration

### Firewall Rules

**Ubuntu (UFW)**
```bash
sudo ufw allow 5000/tcp
sudo ufw allow 8000/tcp  # Dashboard
sudo ufw enable
```

**Windows Firewall**
```powershell
New-NetFirewallRule -DisplayName "Nexis P2P" -Direction Inbound -LocalPort 5000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Nexis Dashboard" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
```

### Reverse Proxy (Nginx)

For production dashboard access:

```nginx
server {
    listen 80;
    server_name nexis.yourdomain.com;

    location / {
        proxy_pass http://localhost:8000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

Enable HTTPS with Let's Encrypt:
```bash
sudo certbot --nginx -d nexis.yourdomain.com
```

## Configuration

### Environment Variables

Create `.env` file (or set system variables):

```
NEXIS_PORT=5000
NEXIS_DASHBOARD_PORT=8000
NEX IS_PEER_ADDRESS=peer1.nexis.network:5000,peer2.nexis.network:5000
NEXIS_PRIVATE_NETWORK=false
NEXIS_MAX_PEERS=50
```

### Private Network Mode

For enterprise deployments, enable private network:

```java
// In NetworkConfig
NetworkConfig.getInstance().setPrivateNetwork(true);
```

Or via API (with admin key):
```bash
curl -X POST http://localhost:8000/api/admin/config \
  -H "X-API-KEY: NEXIS_ADMIN_KEY_001"
```

## Monitoring

### Health Checks

**API Health Endpoint**
```bash
curl http://localhost:8000/api/stats
```

Expected response:
```json
{
  "height": 1234,
  "supply": 61700.0,
  "mempoolSize": 5,
  "peerCount": 3,
  "port": 5000
}
```

### Prometheus Metrics (Future)

Add to your monitoring stack:
```yaml
scrape_configs:
  - job_name: 'nexis-node'
    static_configs:
      - targets: ['localhost:9090']
```

## Backup & Recovery

### Blockchain Data

Backup essential files:
```bash
# Data files
nexis_chain.json
revenue_stats.json

# Configuration (if customized)
.env
```

**Backup Script**
```bash
#!/bin/bash
BACKUP_DIR="/backup/nexis/$(date +%Y%m%d)"
mkdir -p $BACKUP_DIR
cp nexis_chain.json $BACKUP_DIR/
cp revenue_stats.json $BACKUP_DIR/
tar -czf $BACKUP_DIR/nexis-backup.tar.gz $BACKUP_DIR/*.json
```

### Recovery

```bash
# Stop node
sudo systemctl stop nexis-node

# Restore data
cp /backup/nexis/20260122/nexis_chain.json ./
cp /backup/nexis/20260122/revenue_stats.json ./

# Start node
sudo systemctl start nexis-node
```

## Scaling

### Horizontal Scaling

Run multiple nodes behind a load balancer:

```nginx
upstream nexis_nodes {
    server 10.0.1.10:8000;
    server 10.0.1.11:8000;
    server 10.0.1.12:8000;
}

server {
    listen 80;
    location /api/ {
        proxy_pass http://nexis_nodes;
    }
}
```

### Validator Nodes

For PoS validators:
1. Ensure 24/7 uptime
2. Stake minimum 100 NXS
3. Use SSD storage
4. Monitor via dashboard alerts

## Security Best Practices

### Production Checklist

- [ ] Run as non-root user
- [ ] Enable firewall (only necessary ports)
- [ ] Use HTTPS for dashboard (via reverse proxy)
- [ ] Rotate API keys monthly
- [ ] Enable rate limiting
- [ ] Regular backups automated
- [ ] Monitor logs for anomalies
- [ ] Keep Java & dependencies updated
- [ ] Use strong SSH keys (disable password auth)
- [ ] Enable fail2ban for SSH protection

### API Key Management

Generate secure API keys:
```bash
openssl rand -hex 32
```

Store in secure location (e.g., HashiCorp Vault, AWS Secrets Manager)

## Troubleshooting

### Node Won't Start
```bash
# Check Java version
java -version  # Must be 17+

# Check ports availability
lsof -i :5000
netstat -an | grep 5000

# Review logs
sudo journalctl -u nexis-node --since "1 hour ago"
```

### Peers Not Connecting
- Verify firewall rules
- Check peer addresses in configuration
- Ensure nodes are on same network (not mixed private/public)

### Dashboard Not Loading
```bash
# Check if HTTP server started
curl http://localhost:8000/api/stats

# Verify dashboard files exist
ls src/main/resources/dashboard/
```

### Blockchain Corruption
```bash
# Validate chain
# Via CLI, run: chain

# If invalid, restore from backup or resync from peers
rm nexis_chain.json
# Restart node - it will sync from network
```

## Performance Tuning

### JVM Options

For production with 8GB RAM:
```bash
java -Xmx6G -Xms4G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode
```

### Database Optimization

Use SSD storage and ensure:
- Write caching enabled
- Filesystem: ext4 (Linux) or NTFS (Windows)
- Disable indexing on blockchain data directory

## Managed Hosting

For customers who want managed infrastructure:

**Pricing Tiers**
- **Starter**: $99/month - Single node, 99.5% uptime
- **Professional**: $299/month - Multi-node, 99.9% uptime, monitoring
- **Enterprise**: Custom - Private network, SLA, 24/7 support

Contact: [support@nexis.network](mailto:support@nexis.network)

## Support

- **Documentation**: https://github.com/Jamal-Chak/Nexis-NXS-/tree/main/docs
- **Issues**: https://github.com/Jamal-Chak/Nexis-NXS-/issues
- **Community**: Discord (coming soon)
- **Enterprise**: enterprise@nexis.network

---

**Last Updated**: 2026-01-22  
**Version**: 0.1.0-SNAPSHOT
