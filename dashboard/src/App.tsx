import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import { Activity, Database, Users, Server, Zap } from 'lucide-react'
import './App.css'

interface Stats {
    height: number
    supply: number
    mempoolSize: number
    peerCount: number
    port: number
}

interface Block {
    index: number
    hash: string
    previousHash: string
    timestamp: number
    transactions: any[]
    validator?: string
}

interface Transaction {
    transactionId: string
    value: number
    fee: number
}

function App() {
    const [stats, setStats] = useState<Stats | null>(null)
    const [blocks, setBlocks] = useState<Block[]>([])
    const [mempool, setMempool] = useState<Transaction[]>([])

    const fetchData = async () => {
        try {
            const statsRes = await fetch('/api/stats')
            const statsData = await statsRes.json()
            setStats(statsData)

            const chainRes = await fetch('/api/chain')
            const chainData = await chainRes.json()
            setBlocks(chainData.reverse())

            const mempoolRes = await fetch('/api/mempool')
            const mempoolData = await mempoolRes.json()
            setMempool(mempoolData)
        } catch (e) {
            console.error("Failed to fetch data", e)
        }
    }

    useEffect(() => {
        fetchData()
        const interval = setInterval(fetchData, 3000)
        return () => clearInterval(interval)
    }, [])

    return (
        <div className="dashboard">
            <header className="header">
                <div className="logo">
                    <Zap className="logo-icon" />
                    <span>NEXIS <span className="dim">EXPLORER</span></span>
                </div>
                <div className="status">
                    <div className="status-dot pulse"></div>
                    NODE ONLINE
                </div>
            </header>

            <div className="stats-grid">
                <StatCard
                    icon={<Activity />}
                    label="Block Height"
                    value={stats?.height ?? '---'}
                />
                <StatCard
                    icon={<Database />}
                    label="Total Supply"
                    value={stats ? `${stats.supply.toLocaleString()} NXS` : '---'}
                />
                <StatCard
                    icon={<Users />}
                    label="Peers"
                    value={stats?.peerCount ?? '---'}
                />
                <StatCard
                    icon={<Server />}
                    label="Node Port"
                    value={stats?.port ?? '---'}
                />
            </div>

            <div className="main-content">
                <section className="panel blocks-panel">
                    <div className="panel-header">
                        <h2>Recent Blocks</h2>
                    </div>
                    <div className="block-list">
                        {blocks.map((block) => (
                            <motion.div
                                key={block.index}
                                initial={{ opacity: 0, x: -20 }}
                                animate={{ opacity: 1, x: 0 }}
                                className="block-item"
                            >
                                <div className="block-index">#{block.index}</div>
                                <div className="block-info">
                                    <div className="block-hash">{block.hash}</div>
                                    <div className="block-meta">
                                        {new Date(block.timestamp).toLocaleTimeString()} • {block.transactions.length} Transactions
                                    </div>
                                </div>
                                <div className="tx-badge">{block.transactions.length} TXs</div>
                            </motion.div>
                        ))}
                    </div>
                </section>

                <section className="panel mempool-panel">
                    <div className="panel-header">
                        <h2>Mempool</h2>
                        <span className="count-badge">{mempool.length}</span>
                    </div>
                    <div className="mempool-list">
                        {mempool.length === 0 ? (
                            <div className="empty-state">No pending transactions</div>
                        ) : (
                            mempool.map((tx) => (
                                <div key={tx.transactionId} className="tx-item">
                                    <div className="tx-id">{tx.transactionId.substring(0, 16)}...</div>
                                    <div className="tx-details">
                                        <span>{tx.value} NXS</span>
                                        <span className="dim">Fee: {tx.fee}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </section>
            </div>
        </div>
    )
}

function StatCard({ icon, label, value }: { icon: React.ReactNode, label: string, value: string | number }) {
    return (
        <motion.div
            whileHover={{ y: -5 }}
            className="stat-card"
        >
            <div className="stat-icon">{icon}</div>
            <div className="stat-content">
                <div className="stat-label">{label}</div>
                <div className="stat-value">{value}</div>
            </div>
        </motion.div>
    )
}

export default App
