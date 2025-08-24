import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function CustomerLogin() {
  const [cardNumber, setCardNumber] = useState('4123456789012345')
  const [pin, setPin] = useState('1234')
  const [msg, setMsg] = useState('')
  const nav = useNavigate()

  async function onLogin(e) {
    e.preventDefault()
    setMsg('')
    try {
      const res = await fetch('http://localhost:8082/auth/login', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({cardNumber, pin})
      })
      const data = await res.json()
      if (res.ok && data.ok) {
        localStorage.setItem('cardNumber', data.cardNumber)
        nav('/customer')
      } else {
        setMsg(data.message || 'Login failed')
      }
    } catch (e) {
      setMsg('Server error: ' + e.message)
    }
  }

  return (
    <div style={{maxWidth:400}}>
      <h3>Customer Login</h3>
      <form onSubmit={onLogin} style={{display:'grid', gap:8}}>
        <label>Card Number
          <input value={cardNumber} onChange={e=>setCardNumber(e.target.value)} required />
        </label>
        <label>PIN
          <input type="password" value={pin} onChange={e=>setPin(e.target.value)} required />
        </label>
        <button type="submit">Login</button>
      </form>
      {msg && <p style={{color:'crimson'}}>{msg}</p>}
      <p style={{opacity:.7, marginTop:8}}>Tip: seeded card 4123...2345 / PIN 1234</p>
    </div>
  )
}
