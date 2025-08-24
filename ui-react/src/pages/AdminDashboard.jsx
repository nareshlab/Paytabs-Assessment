import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function AdminDashboard() {
  const nav = useNavigate()
  const [txns, setTxns] = useState([])

  useEffect(() => {
    const isAdmin = localStorage.getItem('isAdmin') === 'true'
    if (!isAdmin) {
      nav('/admin')
      return
    }
    ; (async () => {
      try {
        const res = await fetch('http://localhost:8082/admin/transactions')
        const data = await res.json()
        setTxns(Array.isArray(data) ? data : [])
      } catch (e) {
        setTxns([])
      }
    })()
  }, [])

  return (
    <div style={{ minHeight: '100vh', background: '#F5F7FA', fontFamily: 'Segoe UI, Arial, sans-serif' }}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: 32 }}>
        <img src="https://paytabs.com/wp-content/uploads/2017/05/pt-products-tinified@2x-1024x846.png" alt="PayTabs Logo" style={{ width: 80, marginBottom: 16 }} />
        <h3 style={{ color: '#0072CE', fontWeight: 700, marginBottom: 24 }}>Super Admin - All Transactions</h3>
        {txns.length > 0 ? (
          <table border="0" cellPadding="8" style={{ borderCollapse: 'collapse', background: '#fff', borderRadius: 10, boxShadow: '0 2px 8px #0072ce11', minWidth: 900 }}>
            <thead style={{ background: '#F5F7FA', color: '#0072CE' }}>
              <tr>
                <th>ID</th><th>Card</th><th>Type</th><th>Amount</th><th>Status</th><th>Balance After</th><th>Time</th>
              </tr>
            </thead>
            <tbody>
              {txns.map(t => (
                <tr key={t.id} style={{ borderBottom: '1px solid #E0E6ED' }}>
                  <td>{t.id}</td>
                  <td>{t.cardNumber}</td>
                  <td>{t.type}</td>
                  <td>{t.amount}</td>
                  <td>{t.status}{t.declineReason ? ` (${t.declineReason})` : ''}</td>
                  <td>{t.balanceAfter ?? '-'}</td>
                  <td>{t.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p style={{ color: '#D7263D' }}>No transactions found or error fetching.</p>
        )}
      </div>
    </div>
  )
}
