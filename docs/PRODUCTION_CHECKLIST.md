# Nexis Production Checklist

This checklist ensures your Nexis deployment is secure, reliable, and ready for production use.

## Pre-Deployment

### Build & Testing
- [ ] Run `mvn clean package` - build succeeds
- [ ] Run `mvn test` - all tests pass
- [ ] JAR artifact created: `target/nexis-core-0.1.0-SNAPSHOT.jar`
- [ ] Test local startup: `java -cp target/nexis-core-0.1.0-SNAPSHOT.jar com.nexis.app.NexisNode`
- [ ] Verify dashboard loads: http://localhost:8000

### Environment Setup
- [ ] Java 17+ installed and verified (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Sufficient disk space (minimum 20GB, recommended 100GB)
- [ ] Adequate RAM (minimum 4GB, recommended 8GB+)
- [ ] Network ports available (5000-5100, 8000+)

## Security Hardening

### System Security
- [ ] Running as non-root/non-admin user
- [ ] Firewall configured (only necessary ports open)
- [ ] SSH key-based authentication enabled (disable password auth)
- [ ] fail2ban installed and configured (Linux)
- [ ] System updates applied
- [ ] Antivirus/security software configured (Windows)

### Application Security
- [ ] API keys generated (use `openssl rand -hex 32`)
- [ ] API keys stored securely (environment variables or secrets manager)
- [ ] Admin API key changed from default (`NEXIS_ADMIN_KEY_001`)
- [ ] Rate limiting tested and functional
- [ ] HTTPS configured for dashboard (via reverse proxy)
- [ ] CORS settings reviewed and restricted if needed

### Network Security
- [ ] Private network mode configured if applicable
- [ ] Validator whitelist updated (if using permissioned mining)
- [ ] DDoS protection in place (Cloudflare, AWS Shield, etc.)
- [ ] Network segmentation (separate production from development)

## Configuration

### Node Configuration
- [ ] Port configuration set (default 5000)
- [ ] Dashboard port configured (default 8000)
- [ ] Peer addresses configured (for multi-node setup)
- [ ] JVM heap size optimized (`-Xmx` and `-Xms` flags)
- [ ] GC tuning applied (UseG1GC recommended)

### Production Settings
- [ ] Private network mode: `NetworkConfig.setPrivateNetwork(true/false)`
- [ ] Treasury address verified
- [ ] Block reward confirmed (50 NXS)
- [ ] Fee model reviewed and adjusted if needed
- [ ] Logging level set appropriately (INFO for production)

## Deployment

### Service Setup
**Linux (systemd):**
- [ ] Systemd service file created at `/etc/systemd/system/nexis-node.service`
- [ ] Service enabled: `sudo systemctl enable nexis-node`
- [ ] Service started: `sudo systemctl start nexis-node`
- [ ] Service status verified: `sudo systemctl status nexis-node`
- [ ] Logs accessible: `sudo journalctl -u nexis-node -f`

**Windows:**
- [ ] NSSM service installed
- [ ] Service registered and started
- [ ] Service set to auto-start
- [ ] Event log monitoring configured

### Reverse Proxy (Optional but Recommended)
- [ ] Nginx/Apache installed and configured
- [ ] SSL certificate obtained (Let's Encrypt)
- [ ] HTTPS enabled and tested
- [ ] HTTP to HTTPS redirect configured
- [ ] Reverse proxy serving dashboard correctly

### Load Balancer (Multi-Node)
- [ ] Load balancer configured (if running multiple nodes)
- [ ] Health checks enabled
- [ ] Session affinity configured if needed
- [ ] Failover tested

## Monitoring & Observability

### Health Monitoring
- [ ] API health endpoint tested: `curl http://localhost:8000/api/stats`
- [ ] Dashboard accessible and loading data
- [ ] Uptime monitoring configured (UptimeRobot, Pingdom, etc.)
- [ ] Alerting configured for downtime

### Logging
- [ ] Log directory created and writable
- [ ] Log rotation configured (logrotate on Linux)
- [ ] Centralized logging setup (optional: ELK, Splunk, CloudWatch)
- [ ] Error logs monitored

### Metrics (Future)
- [ ] Prometheus endpoint configured (if available)
- [ ] Grafana dashboards created
- [ ] Key metrics tracked:
  - [ ] Transactions per second
  - [ ] Block production rate
  - [ ] Peer count
  - [ ] Memory usage
  - [ ] API response time

## Backup & Recovery

### Backup Strategy
- [ ] Backup script created
- [ ] Automated backups scheduled (daily recommended)
- [ ] Backup includes:
  - [ ] `nexis_chain.json`
  - [ ] `revenue_stats.json`
  - [ ] Configuration files
- [ ] Backups stored offsite (S3, Google Cloud Storage, etc.)
- [ ] Backup retention policy defined (e.g., keep 30 days)

### Recovery Testing
- [ ] Recovery procedure documented
- [ ] Test recovery performed and successful
- [ ] Recovery time objective (RTO) defined
- [ ] Recovery point objective (RPO) defined

## Performance

### Load Testing
- [ ] Baseline performance measured
- [ ] Load testing completed (transaction volume)
- [ ] Stress testing completed (resource limits)
- [ ] Performance bottlenecks identified and resolved

### Optimization
- [ ] JVM performance tuned
- [ ] Database/storage optimized (SSD recommended)
- [ ] Network latency measured
- [ ] API response times acceptable (<100ms for critical endpoints)

## Documentation

### Internal Documentation
- [ ] Deployment procedure documented
- [ ] Runbook created (troubleshooting steps)
- [ ] Escalation procedures defined
- [ ] Architecture diagram created
- [ ] Network topology documented

### User Documentation
- [ ] API documentation published
- [ ] User guide created (if applicable)
- [ ] FAQ updated
- [ ] Support channels defined

## Business Continuity

### High Availability
- [ ] Multi-node deployment configured
- [ ] Geographic redundancy considered
- [ ] Failover tested
- [ ] Load balancing verified

### Disaster Recovery
- [ ] DR plan documented
- [ ] DR drills scheduled
- [ ] Backup site identified (if applicable)
- [ ] Communication plan for outages

## Compliance & Legal

### Data Protection
- [ ] Data retention policy defined
- [ ] Privacy policy reviewed (if handling user data)
- [ ] GDPR compliance verified (if applicable in EU)
- [ ] Data encryption at rest enabled (if required)

### Financial Compliance
- [ ] Regulatory requirements reviewed
- [ ] KYC/AML considered (if accepting real funds)
- [ ] Terms of service published
- [ ] Legal disclaimer added to dashboard

## Post-Deployment

### Verification
- [ ] All nodes synced (multi-node setup)
- [ ] Dashboard displaying correct data
- [ ] API endpoints responding correctly
- [ ] Transactions processing successfully
- [ ] Fees being collected and distributed

### Monitoring
- [ ] First 24 hours monitored closely
- [ ] No critical errors in logs
- [ ] Performance metrics within acceptable range
- [ ] Resource usage stable

### Communication
- [ ] Operations team notified of deployment
- [ ] Users/customers notified (if applicable)
- [ ] Support team trained
- [ ] Status page updated

## Ongoing Maintenance

### Regular Tasks
- [ ] Weekly log review
- [ ] Monthly backup verification
- [ ] Quarterly security audit
- [ ] Quarterly performance review
- [ ] Dependencies updated regularly

### Updates & Patches
- [ ] Update procedure defined
- [ ] Test environment for updates
- [ ] Rollback procedure documented
- [ ] Change management process in place

---

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| DevOps | | | |
| Security | | | |
| Manager | | | |

---

**Deployment Date**: _______________  
**Production URL**: _______________  
**Support Contact**: _______________

**Notes**:
_______________________________________
_______________________________________
_______________________________________
