import React from 'react'
import ReactDOM from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import App from './App.jsx'
import CustomerLogin from './pages/CustomerLogin.jsx'
import CustomerDashboard from './pages/CustomerDashboard.jsx'
import AdminLogin from './pages/AdminLogin.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'

const router = createBrowserRouter([
  { path: '/', element: <App />,
    children: [
      { index: true, element: <CustomerLogin /> },
      { path: 'customer', element: <CustomerDashboard /> },
      { path: 'admin', element: <AdminLogin /> },
      { path: 'admin/dashboard', element: <AdminDashboard /> }
    ]
  }
])

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
)
