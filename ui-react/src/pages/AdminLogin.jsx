import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function AdminLogin() {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin')
  const [msg, setMsg] = useState('')
  const nav = useNavigate()

  async function onLogin(e) {
    e.preventDefault()
    setMsg('')
    try {
      const res = await fetch('http://localhost:8082/auth/adminLogin', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({username, password})
      })
      const data = await res.json()
      if (res.ok && data.ok) {
        localStorage.setItem('isAdmin', 'true')
        nav('/admin/dashboard')
      } else {
        setMsg(data.message || 'Login failed')
      }
    } catch (e) {
      setMsg('Server error: ' + e.message)
    }
  }

  return (
    <div style={{maxWidth:400}}>
      <h3>Admin Login</h3>
      <form onSubmit={onLogin} style={{display:'grid', gap:8}}>
        <label>Username
          <input value={username} onChange={e=>setUsername(e.target.value)} required />
        </label>
        <label>Password
          <input type="password" value={password} onChange={e=>setPassword(e.target.value)} required />
        </label>
        <button type="submit">Login</button>
      </form>
      {msg && <p style={{color:'crimson'}}>{msg}</p>}
      <p style={{opacity:.7, marginTop:8}}>Tip: admin / admin (POC only)</p>
    </div>
  )
}
