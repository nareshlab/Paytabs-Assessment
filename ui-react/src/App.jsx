import React from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'

export default function App() {
  const loc = useLocation()
  return (
    <div style={{fontFamily:'system-ui, sans-serif', margin:'24px'}}>
      <header style={{display:'flex', gap:'16px', alignItems:'center', marginBottom:'16px'}}>
        <h2>🏦 Banking POC</h2>
        <nav style={{display:'flex', gap:'12px'}}>
          <Link to="/">Customer</Link>
          <Link to="/admin">Admin</Link>
        </nav>
        <div style={{marginLeft:'auto', opacity:0.7}}>{loc.pathname}</div>
      </header>
      <Outlet />
    </div>
  )
}
