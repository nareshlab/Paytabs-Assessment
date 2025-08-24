import React, { useEffect, useState } from 'react'

export default function CustomerDashboard() {
  const cardNumber = localStorage.getItem('cardNumber')
  const [balance, setBalance] = useState(null)
  const [txns, setTxns] = useState([])
  const [amount, setAmount] = useState('100.00')
  const [pin, setPin] = useState('')
  const [msg, setMsg] = useState('')

  async function withdraw() {
    setMsg('')
    if (!pin || pin.trim() === '') {
      setMsg('Please enter PIN')
      return
    }
    const amt = parseFloat(amount)
    if (isNaN(amt) || amt <= 0) {
      setMsg('Amount must be greater than 0')
      return
    }
    try {
      const res = await fetch('http://localhost:8081/transaction', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          cardNumber,
          pin,
          amount: amt,
          type: 'withdraw'
        })
      })
      const data = await res.json()
      setMsg(data.message || 'Transaction done')
      await load()
    } catch (e) {
      setMsg('Server error: ' + e.message)
    } finally {
      // Clear PIN after operation completes (success or failure)
      setPin('')
    }
  }

  async function load() {
    if (!cardNumber) return

    try {
      // Fetch balance
      const b = await fetch(`http://localhost:8082/customer/${cardNumber}/balance`)
      const bData = await b.json()
      setBalance(typeof bData === "number" ? bData : null)

      // Fetch transactions
      const t = await fetch(`http://localhost:8082/customer/${cardNumber}/transactions`)
      const tData = await t.json()
      setTxns(Array.isArray(tData) ? tData : [])
    } catch (e) {
      setBalance(null)
      setTxns([])
    }
  }

  useEffect(() => { load() }, [])

  async function topup() {
    setMsg('')
    if (!pin || pin.trim() === '') {
      setMsg('Please enter PIN')
      return
    }
    const amt = parseFloat(amount)
    if (isNaN(amt) || amt <= 0) {
      setMsg('Amount must be greater than 0')
      return
    }
    try {
      const res = await fetch('http://localhost:8081/transaction', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          cardNumber,
          pin,
          amount: amt,
          type: 'topup'
        })
      })
      const data = await res.json()
      setMsg(data.message || 'Transaction done')
      await load()
    } catch (e) {
      setMsg('Server error: ' + e.message)
    } finally {
      // Clear PIN after operation completes
      setPin('')
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: '#F5F7FA', fontFamily: 'Segoe UI, Arial, sans-serif' }}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: 32 }}>
        <img src="https://paytabs.com/wp-content/uploads/2017/05/pt-products-tinified@2x-1024x846.png" alt="PayTabs Logo" style={{ width: 80, marginBottom: 16 }} />
        <h3 style={{ color: '#0072CE', fontWeight: 700, marginBottom: 24 }}>Welcome, card {cardNumber || '(not logged in)'}</h3>
        {cardNumber ? (
          <>
            <div style={{ display: 'flex', gap: 24, marginBottom: 32 }}>
              <div style={{ background: '#fff', border: '1px solid #E0E6ED', borderRadius: 12, padding: 24, minWidth: 220, boxShadow: '0 2px 8px #0072ce11' }}>
                <div style={{ fontSize: 13, color: '#005B9F', marginBottom: 8 }}>Current Balance</div>
                <div style={{ fontSize: 32, color: '#0072CE', fontWeight: 600 }}>
                  {balance !== null ? `₹${balance}` : "Error fetching balance"}
                </div>
              </div>
              <div style={{ background: '#fff', border: '1px solid #E0E6ED', borderRadius: 12, padding: 24, minWidth: 320, boxShadow: '0 2px 8px #0072ce11' }}>
                <div style={{ fontSize: 13, color: '#005B9F', marginBottom: 10 }}>Top-up / Withdraw</div>
                <div style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 8 }}>
                  <input type="number" value={amount} onChange={e => setAmount(e.target.value)} step="0.01" style={{ padding: 8, borderRadius: 6, border: '1px solid #E0E6ED', width: 90 }} />
                  <input type="password" placeholder="PIN" value={pin} onChange={e => setPin(e.target.value)} style={{ padding: 8, borderRadius: 6, border: '1px solid #E0E6ED', width: 90 }} />
                  <button onClick={topup} style={{ background: '#0072CE', color: '#fff', border: 'none', borderRadius: 6, padding: '8px 16px', fontWeight: 600, cursor: 'pointer' }}>Top-up</button>
                  <button onClick={withdraw} style={{ background: '#005B9F', color: '#fff', border: 'none', borderRadius: 6, padding: '8px 16px', fontWeight: 600, cursor: 'pointer' }}>Withdraw</button>
                </div>
                {msg && <div style={{ marginTop: 6, color: msg.includes('error') ? '#D7263D' : '#0072CE' }}>{msg}</div>}
              </div>
            </div>
            <h4 style={{ marginTop: 0, color: '#005B9F', fontWeight: 600 }}>Transactions</h4>
            {txns.length > 0 ? (
              <table border="0" cellPadding="8" style={{ borderCollapse: 'collapse', background: '#fff', borderRadius: 10, boxShadow: '0 2px 8px #0072ce11', minWidth: 700 }}>
                <thead style={{ background: '#F5F7FA', color: '#0072CE' }}>
                  <tr><th>ID</th><th>Type</th><th>Amount</th><th>Status</th><th>Balance After</th><th>Time</th></tr>
                </thead>
                <tbody>
                  {txns.map(t => (
                    <tr key={t.id} style={{ borderBottom: '1px solid #E0E6ED' }}>
                      <td>{t.id}</td>
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
          </>
        ) : (
          <p style={{ color: '#D7263D' }}>Please go to Home and login first.</p>
        )}
      </div>
    </div>
  )
}
